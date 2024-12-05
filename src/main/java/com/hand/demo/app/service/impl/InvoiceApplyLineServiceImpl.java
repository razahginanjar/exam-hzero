package com.hand.demo.app.service.impl;

import com.hand.demo.app.service.InvoiceApplyHeaderService;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.Constants;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyLineService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;

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

    @Override
    public Page<InvoiceApplyLine> selectList(PageRequest pageRequest, InvoiceApplyLine invoiceApplyLine) {
        return PageHelper
                .doPageAndSort(pageRequest,
                        () -> invoiceApplyLineRepository.selectList(invoiceApplyLine));
    }

    @Override
    public void saveData(List<InvoiceApplyLine> invoiceApplyLines) {
        Map<Long, InvoiceApplyHeader> headerCache = new HashMap<>();
        List<InvoiceApplyLine> insertList = new ArrayList<>();
        List<InvoiceApplyLine> updateList = new ArrayList<>();

        for (InvoiceApplyLine line : invoiceApplyLines) {
            calculateLineAmounts(line);

            if (line.getApplyHeaderId() == null) {
                throw new CommonException(Constants.MESSAGE_ERROR_HEADER_ID_CANNOT_BE_NULL);
            }

            InvoiceApplyHeader header = fetchHeader(line.getApplyHeaderId(), headerCache);

            if (line.getApplyLineId() == null) {
                // New line
                updateHeaderAmounts(header, line.getTotalAmount(), line.getTaxAmount(), line.getExcludeTaxAmount());
                insertList.add(line);
            } else {
                // Existing line
                InvoiceApplyLine existingLine = invoiceApplyLineRepository.selectByPrimary(line.getApplyLineId());
                if (existingLine == null) {
                    throw new CommonException(Constants.MESSAGE_ERROR_INV_LINE_NOT_FOUND);
                }

                updateHeaderAmounts(header,
                        line.getTotalAmount().subtract(existingLine.getTotalAmount()),
                        line.getTaxAmount().subtract(existingLine.getTaxAmount()),
                        line.getExcludeTaxAmount().subtract(existingLine.getExcludeTaxAmount()));
                updateList.add(line);
            }
        }

        // Persist changes
        if (!insertList.isEmpty()) {
            invoiceApplyLineRepository.batchInsertSelective(insertList);
        }
        if (!updateList.isEmpty()) {
            invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateList);
        }

        // Update headers in batch
        for (InvoiceApplyHeader header : headerCache.values()) {
            invoiceApplyHeaderRepository.updateByPrimaryKeySelective(header);
        }
    }

    private void calculateLineAmounts(InvoiceApplyLine line) {
        BigDecimal totalAmount = line.getUnitPrice().multiply(line.getQuantity());
        BigDecimal taxAmount = totalAmount.multiply(line.getTaxRate());
        BigDecimal excludeTaxAmount = totalAmount.subtract(taxAmount);

        line.setTotalAmount(totalAmount);
        line.setTaxAmount(taxAmount);
        line.setExcludeTaxAmount(excludeTaxAmount);
    }

    private void updateHeaderAmounts(InvoiceApplyHeader header, BigDecimal totalDelta, BigDecimal taxDelta, BigDecimal excludeDelta) {
        header.setTotalAmount(header.getTotalAmount().add(totalDelta));
        header.setTaxAmount(header.getTaxAmount().add(taxDelta));
        header.setExcludeTaxAmount(header.getExcludeTaxAmount().add(excludeDelta));
    }

    private InvoiceApplyHeader fetchHeader(Long headerId, Map<Long, InvoiceApplyHeader> headerCache) {
        if (!headerCache.containsKey(headerId)) {
            InvoiceApplyHeader request = new InvoiceApplyHeader();
            request.setApplyHeaderId(headerId);
            request.setDelFlag(0);
            InvoiceApplyHeader fetchedHeader = invoiceApplyHeaderRepository.selectOne(request);
            if (fetchedHeader == null) {
                throw new CommonException(Constants.MESSAGE_ERROR_NOT_FOUND, headerId);
            }
            headerCache.put(headerId, fetchedHeader);
        }
        return headerCache.get(headerId);
    }



    @Override
    public void removeData(List<InvoiceApplyLine> invoiceApplyLines) {
//        for (InvoiceApplyLine invoiceApplyLine : invoiceApplyLines) {
//            InvoiceApplyLine invoiceApplyLine1
//                    = invoiceApplyLineRepository.selectOne(invoiceApplyLine);
//            if(Objects.isNull(invoiceApplyLine1))
//            {
//                throw new CommonException(Constants.MESSAGE_ERROR_INV_LINE_NOT_FOUND);
//            }
//            InvoiceApplyHeader invoiceApplyHeader =
//                    invoiceApplyHeaderRepository.selectByPrimary(invoiceApplyLine1.getApplyHeaderId());
//            BigDecimal excludeTaxAmount = invoiceApplyHeader.getExcludeTaxAmount();
//            BigDecimal totalAmount = invoiceApplyHeader.getTotalAmount();
//            BigDecimal taxAmount = invoiceApplyHeader.getTaxAmount();
//
//            taxAmount = taxAmount.subtract(invoiceApplyLine1.getTaxAmount());
//            totalAmount = totalAmount.subtract(invoiceApplyLine1.getTotalAmount());
//            excludeTaxAmount = excludeTaxAmount.subtract(invoiceApplyLine1.getExcludeTaxAmount());
//
//
//            invoiceApplyHeader.setTotalAmount(totalAmount);
//            invoiceApplyHeader.setTaxAmount(taxAmount);
//            invoiceApplyHeader.setExcludeTaxAmount(excludeTaxAmount);
//            invoiceApplyHeaderRepository.updateByPrimaryKeySelective(invoiceApplyHeader);
//            invoiceApplyLineRepository.delete(invoiceApplyLine1);
//        }
        Map<Long, InvoiceApplyHeader> headerCache = new HashMap<>();

        for (InvoiceApplyLine line : invoiceApplyLines) {
            InvoiceApplyLine existingLine = invoiceApplyLineRepository.selectOne(line);
            if (existingLine == null) {
                throw new CommonException(Constants.MESSAGE_ERROR_INV_LINE_NOT_FOUND);
            }

            InvoiceApplyHeader header = fetchHeader(existingLine.getApplyHeaderId(), headerCache);

            updateHeaderAmounts(header,
                    existingLine.getTotalAmount().negate(),
                    existingLine.getTaxAmount().negate(),
                    existingLine.getExcludeTaxAmount().negate());

            invoiceApplyLineRepository.delete(existingLine);
        }

        // Update headers in batch
        for (InvoiceApplyHeader header : headerCache.values()) {
            invoiceApplyHeaderRepository.updateByPrimaryKeySelective(header);
        }
    }

    @Override
    public List<InvoiceApplyLine> selectByInvoiceHeader(Long headerId) {
        InvoiceApplyLine invoiceApplyLine = new InvoiceApplyLine();
        invoiceApplyLine.setApplyLineId(headerId);
        return invoiceApplyLineRepository.selectList(invoiceApplyLine);
    }
}

