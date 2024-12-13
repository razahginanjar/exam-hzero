package com.hand.demo.app.service.impl;


import com.hand.demo.api.dto.InvApplyHeaderDTO;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.Constants;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.hzero.core.redis.RedisHelper;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyLineService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Invoice Apply Line Table(InvoiceApplyLine)应用服务
 *
 * @author razah
 * @since 2024-12-03 09:27:59
 */
@Service
public class InvoiceApplyLineServiceImpl implements InvoiceApplyLineService {
    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    private RedisHelper redisHelper;

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceApplyLine> selectList(PageRequest pageRequest, InvoiceApplyLine invoiceApplyLine) {
        return PageHelper
                .doPageAndSort(pageRequest,
                        () -> invoiceApplyLineRepository.selectList(invoiceApplyLine));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveData(List<InvoiceApplyLine> invoiceApplyLines) {
        //calculate amounts and validate
        for (InvoiceApplyLine invoiceApplyLine : invoiceApplyLines) {
            calculateLineAmounts(invoiceApplyLine);
            // Ensure the header exists for the line
            if (invoiceApplyLine.getApplyHeaderId() == null) {
                throw new CommonException(Constants.MESSAGE_ERROR_HEADER_ID_CANNOT_BE_NULL);
            }
        }

        // Retrieve all necessary headers and lines once at the beginning
        Map<Long, InvoiceApplyHeader> headerCache = new HashMap<>();

        Map<Long, InvoiceApplyHeader> headerMap = getHeadersByLines(invoiceApplyLines);


        // Prepare lists for insert and update
        List<InvoiceApplyLine> insertList = invoiceApplyLines.stream()
                .filter(line -> line.getApplyLineId() == null)
                .collect(Collectors.toList());
        List<InvoiceApplyLine> updateList = invoiceApplyLines.stream()
                .filter(line -> line.getApplyLineId() != null)
                .collect(Collectors.toList());

        updateData(updateList, headerMap, headerCache);
        insertData(insertList, headerMap, headerCache);

        // Batch update headers at the end
        List<InvoiceApplyHeader> headerUpdates = new ArrayList<>(headerCache.values());
        if (!headerUpdates.isEmpty()) {
            invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(headerUpdates);
        }
        List<String> keys = new ArrayList<>();
        headerUpdates.forEach(
                invoiceApplyHeader -> {
                    keys.add(Constants.CACHE_KEY_PREFIX +":"+invoiceApplyHeader.getApplyHeaderId());
                }
        );
        redisHelper.delKeys(keys);
    }

    private void insertData(List<InvoiceApplyLine> requests,
                            Map<Long, InvoiceApplyHeader> headerMap,
                            Map<Long, InvoiceApplyHeader> headerCache)
    {
        if (requests.isEmpty()) {
            return;
        }
        for (InvoiceApplyLine line : requests) {
            InvoiceApplyHeader headerDTO = headerMap.get(line.getApplyHeaderId());
            if (headerDTO == null) {
                throw new CommonException(Constants.MESSAGE_ERROR_NOT_FOUND);
            }

            // Use cache to retrieve or set the header
            InvoiceApplyHeader header = headerCache.computeIfAbsent(line.getApplyHeaderId(),
                    id -> headerDTO);
            // New line: Insert operation
            updateHeaderAmounts(header, line.getTotalAmount(), line.getTaxAmount(), line.getExcludeTaxAmount());
            // Ensure header is updated in the cache
            headerCache.put(line.getApplyHeaderId(), header);
        }
        invoiceApplyLineRepository.batchInsertSelective(requests);
    }


    //update line data
    private void updateData(List<InvoiceApplyLine> requests,
                                              Map<Long, InvoiceApplyHeader> headerMap,
                                              Map<Long, InvoiceApplyHeader> headerCache)
    {
        if (requests.isEmpty()) {
            return;
        }
        Set<String> headerIds = requests.stream()
                .map(header -> header.getApplyLineId().toString())
                .collect(Collectors.toSet());
        Map<Long, InvoiceApplyLine> lineMap = invoiceApplyLineRepository
                .selectByIds(String.join(",", headerIds)).stream()
                .collect(Collectors.toMap(InvoiceApplyLine::getApplyLineId, line -> line));
        for (InvoiceApplyLine line : requests) {

            // Ensure the header exists for the line
            if (line.getApplyHeaderId() == null) {
                throw new CommonException(Constants.MESSAGE_ERROR_HEADER_ID_CANNOT_BE_NULL);
            }

            InvoiceApplyHeader headerDTO = headerMap.get(line.getApplyHeaderId());
            if (headerDTO == null) {
                throw new CommonException(Constants.MESSAGE_ERROR_NOT_FOUND);
            }

            // Use cache to retrieve or set the header
            InvoiceApplyHeader header = headerCache.computeIfAbsent(line.getApplyHeaderId(),
                    id -> headerDTO);

                // Existing line: Update operation
            InvoiceApplyLine existingLine = lineMap.get(line.getApplyLineId());
            if (existingLine == null) {
                throw new CommonException(Constants.MESSAGE_ERROR_INV_LINE_NOT_FOUND);
            }

            updateHeaderAmounts(header,
                        line.getTotalAmount().subtract(existingLine.getTotalAmount()),
                        line.getTaxAmount().subtract(existingLine.getTaxAmount()),
                        line.getExcludeTaxAmount().subtract(existingLine.getExcludeTaxAmount()));
            // Ensure header is updated in the cache
            headerCache.put(line.getApplyHeaderId(), header);

            if(line.getQuantity() == null)
            {
                line.setQuantity(existingLine.getQuantity());
            }
            if(line.getTaxRate() == null)
            {
                line.setTaxRate(existingLine.getTaxRate());
            }
            if(line.getContentName() == null)
            {
                line.setContentName(existingLine.getContentName());
            }
            if(line.getInvoiceName() == null)
            {
                line.setInvoiceName(existingLine.getInvoiceName());
            }
            if(line.getUnitPrice() == null)
            {
                line.setUnitPrice(existingLine.getUnitPrice());
            }
            if(line.getTaxClassificationNumber() == null)
            {
                line.setTaxClassificationNumber(existingLine.getTaxClassificationNumber());
            }
            if(line.getRemark() == null)
            {
                line.setRemark(existingLine.getRemark());
            }

        }

        invoiceApplyLineRepository.batchUpdateOptional(
                requests,
                InvoiceApplyLine.FIELD_INVOICE_NAME,
                InvoiceApplyLine.FIELD_CONTENT_NAME,
                InvoiceApplyLine.FIELD_QUANTITY,
                InvoiceApplyLine.FIELD_REMARK,
                InvoiceApplyLine.FIELD_TAX_CLASSIFICATION_NUMBER,
                InvoiceApplyLine.FIELD_TAX_RATE,
                InvoiceApplyLine.FIELD_UNIT_PRICE
        );
    }



    //calculate total, tax, exclude amount
    private void calculateLineAmounts(InvoiceApplyLine line) {
        BigDecimal totalAmount = line.getUnitPrice().multiply(line.getQuantity());
        BigDecimal taxAmount = totalAmount.multiply(line.getTaxRate());
        BigDecimal excludeTaxAmount = totalAmount.subtract(taxAmount);

        line.setTotalAmount(totalAmount);
        line.setTaxAmount(taxAmount);
        line.setExcludeTaxAmount(excludeTaxAmount);
    }

    //update header total, tax, exclude amount
    private void updateHeaderAmounts(InvoiceApplyHeader header, BigDecimal totalDelta, BigDecimal taxDelta, BigDecimal excludeDelta) {
        header.setTotalAmount(header.getTotalAmount().add(totalDelta));
        header.setTaxAmount(header.getTaxAmount().add(taxDelta));
        header.setExcludeTaxAmount(header.getExcludeTaxAmount().add(excludeDelta));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeData(List<InvoiceApplyLine> invoiceApplyLines) {
        // Retrieve necessary headers and lines once
        Map<Long, InvoiceApplyHeader> headerMap = getHeadersByLines(invoiceApplyLines);

        Set<String> headerIds = invoiceApplyLines.stream()
                .map(header -> header.getApplyLineId().toString())
                .collect(Collectors.toSet());
        Map<Long, InvoiceApplyLine> lineMap = invoiceApplyLineRepository
                .selectByIds(String.join(",", headerIds)).stream()
                .collect(Collectors.toMap(InvoiceApplyLine::getApplyLineId, line -> line));


        // Process each line to update header amounts
        for (InvoiceApplyLine line : invoiceApplyLines) {
            InvoiceApplyLine existingLine = lineMap.get(line.getApplyLineId());
            if (existingLine == null) {
                throw new CommonException(Constants.MESSAGE_ERROR_INV_LINE_NOT_FOUND);
            }

            InvoiceApplyHeader header = headerMap.get(line.getApplyHeaderId());

            if(Objects.isNull(header))
            {
                throw new CommonException(Constants.MESSAGE_ERROR_NOT_FOUND);
            }

            updateHeaderAmounts(header,
                    existingLine.getTotalAmount().negate(),
                    existingLine.getTaxAmount().negate(),
                    existingLine.getExcludeTaxAmount().negate());
        }

        // Batch delete lines and update headers
        invoiceApplyLineRepository
                .batchDeleteByPrimaryKey(invoiceApplyLines);
        invoiceApplyHeaderRepository
                .batchUpdateByPrimaryKeySelective(new ArrayList<>(headerMap.values()));
        List<String> keys = new ArrayList<>();
        headerMap.values().forEach(
                invApplyHeaderDTO -> {
                    keys.add(Constants.CACHE_KEY_PREFIX+":"+invApplyHeaderDTO.getApplyHeaderId());
                }
        );
        redisHelper.delKeys(keys);
    }

    private Map<Long, InvoiceApplyHeader> getHeadersByLines(List<InvoiceApplyLine> invoiceApplyLines) {
        Set<String> headerIds = invoiceApplyLines.stream()
                .map(header -> header.getApplyHeaderId().toString())
                .collect(Collectors.toSet());

        List<InvoiceApplyHeader> invoiceApplyHeaders =
                invoiceApplyHeaderRepository.selectByIds(String.join(",", headerIds));

         return invoiceApplyHeaders.stream()
                .collect(Collectors.toMap(InvoiceApplyHeader::getApplyHeaderId,
                        header -> header));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceApplyLine> selectByInvoiceHeader(Long headerId) {
        InvoiceApplyLine invoiceApplyLine = new InvoiceApplyLine();
        invoiceApplyLine.setApplyHeaderId(headerId);
        return invoiceApplyLineRepository.selectList(invoiceApplyLine);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceApplyLine> selectList(InvoiceApplyLine invoiceApplyLine) {
        return invoiceApplyLineRepository.selectList(invoiceApplyLine);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceApplyLine> selectAll() {
        return invoiceApplyLineRepository.selectAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceApplyLine> exportData(InvoiceApplyLine invoiceApplyLine) {
        return invoiceApplyLineRepository.selectList(invoiceApplyLine);
    }

    @Override
    public List<InvoiceApplyLine> getFromHeaders(List<Long> headerIds) {
        return invoiceApplyLineRepository.selectByHeaderIds(headerIds);
    }
}

