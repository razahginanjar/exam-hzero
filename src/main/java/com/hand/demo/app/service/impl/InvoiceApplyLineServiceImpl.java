package com.hand.demo.app.service.impl;


import com.hand.demo.api.dto.InvApplyHeaderDTO;
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
//    @Autowired
//    private InvoiceApplyLineRepository invoiceApplyLineRepository;
//
//    @Autowired
//    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;
//
//    @Override
//    public Page<InvoiceApplyLine> selectList(PageRequest pageRequest, InvoiceApplyLine invoiceApplyLine) {
//        return PageHelper
//                .doPageAndSort(pageRequest,
//                        () -> invoiceApplyLineRepository.selectList(invoiceApplyLine));
//    }
//
//    @Override
//    public void saveData(List<InvoiceApplyLine> invoiceApplyLines) {
//
//        // Initialize cache for headers and lines
//        Map<Long, InvoiceApplyHeader> headerCache = new HashMap<>();
//        Map<Long, InvoiceApplyLine> lineMap = invoiceApplyLineRepository.selectAll().stream()
//                .collect(Collectors.toMap(InvoiceApplyLine::getApplyLineId, line -> line));
//
//        InvApplyHeaderDTO invApplyHeaderDTO = new InvApplyHeaderDTO();
//        invApplyHeaderDTO.setDelFlag(0);
//        Map<Long, InvApplyHeaderDTO> headerMap = invoiceApplyHeaderRepository.selectList(invApplyHeaderDTO).stream()
//                .collect(Collectors.toMap(InvApplyHeaderDTO::getApplyHeaderId, header -> header));
//
//        // Lists to collect insert and update operations
//        List<InvoiceApplyLine> insertList = new ArrayList<>();
//        List<InvoiceApplyLine> updateList = new ArrayList<>();
//
//        for (InvoiceApplyLine line : invoiceApplyLines) {
//            calculateLineAmounts(line);
//
//            // Ensure the header exists for the line
//            if (line.getApplyHeaderId() == null) {
//                throw new CommonException(Constants.MESSAGE_ERROR_HEADER_ID_CANNOT_BE_NULL);
//            }
//
//            InvApplyHeaderDTO headerDTO = headerMap.get(line.getApplyHeaderId());
//            if (headerDTO == null) {
//                throw new CommonException(Constants.MESSAGE_ERROR_NOT_FOUND);
//            }
//
//            // Retrieve or update the header from cache
//            InvoiceApplyHeader header = headerCache.computeIfAbsent(line.getApplyHeaderId(), id -> headerDTO);
//
//            if (line.getApplyLineId() == null) {
//                // New line: Insert operation
//                updateHeaderAmounts(header, line.getTotalAmount(), line.getTaxAmount(), line.getExcludeTaxAmount());
//                insertList.add(line);
//            } else {
//                // Existing line: Update operation
//                InvoiceApplyLine existingLine = lineMap.get(line.getApplyLineId());
//                if (existingLine == null) {
//                    throw new CommonException(Constants.MESSAGE_ERROR_INV_LINE_NOT_FOUND);
//                }
//
//                updateHeaderAmounts(header,
//                        line.getTotalAmount().subtract(existingLine.getTotalAmount()),
//                        line.getTaxAmount().subtract(existingLine.getTaxAmount()),
//                        line.getExcludeTaxAmount().subtract(existingLine.getExcludeTaxAmount()));
//                updateList.add(line);
//            }
//
//            // Ensure header is updated in the cache
//            headerCache.put(line.getApplyHeaderId(), header);
//        }
//
//        // Persist changes in bulk
//        if (!insertList.isEmpty()) {
//            invoiceApplyLineRepository.batchInsertSelective(insertList);
//        }
//        if (!updateList.isEmpty()) {
//            invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateList);
//        }
//
//        // Batch update headers at the end
//        List<InvoiceApplyHeader> headerUpdates = new ArrayList<>(headerCache.values());
//        if (!headerUpdates.isEmpty()) {
//            invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(headerUpdates);
//        }
//    }
//
//    private void calculateLineAmounts(InvoiceApplyLine line) {
//        BigDecimal totalAmount = line.getUnitPrice().multiply(line.getQuantity());
//        BigDecimal taxAmount = totalAmount.multiply(line.getTaxRate());
//        BigDecimal excludeTaxAmount = totalAmount.subtract(taxAmount);
//
//        line.setTotalAmount(totalAmount);
//        line.setTaxAmount(taxAmount);
//        line.setExcludeTaxAmount(excludeTaxAmount);
//    }
//
//    private void updateHeaderAmounts(InvoiceApplyHeader header, BigDecimal totalDelta, BigDecimal taxDelta, BigDecimal excludeDelta) {
//        header.setTotalAmount(header.getTotalAmount().add(totalDelta));
//        header.setTaxAmount(header.getTaxAmount().add(taxDelta));
//        header.setExcludeTaxAmount(header.getExcludeTaxAmount().add(excludeDelta));
//    }
//
//
//    @Override
//    public void removeData(List<InvoiceApplyLine> invoiceApplyLines) {
//        List<InvApplyHeaderDTO> headerDTOS = invoiceApplyHeaderRepository.selectList(new InvApplyHeaderDTO());
//        Map<Long, InvApplyHeaderDTO> headerMap = headerDTOS.stream()
//                .collect(Collectors.toMap(
//                        InvApplyHeaderDTO::getApplyHeaderId, // Key mapper
//                        dto -> dto,                         // Value mapper
//                        (existing, replacement) -> existing // Merge function in case of duplicate keys
//                ));
//
//        List<InvoiceApplyLine> invoiceApplyLines1 = invoiceApplyLineRepository.selectAll();
//        Map<Long, InvoiceApplyLine> lineMap = invoiceApplyLines1.stream()
//                .collect(Collectors.toMap(
//                        InvoiceApplyLine::getApplyLineId, // Key mapper
//                        dto -> dto,                         // Value mapper
//                        (existing, replacement) -> existing // Merge function in case of duplicate keys
//                ));
//
//        for (InvoiceApplyLine line : invoiceApplyLines) {
//            InvoiceApplyLine existingLine = lineMap.get(line.getApplyLineId());
//            if (existingLine == null) {
//                throw new CommonException(Constants.MESSAGE_ERROR_INV_LINE_NOT_FOUND);
//            }
//
//            InvoiceApplyHeader header = headerMap.get(line.getApplyHeaderId());
//
//            updateHeaderAmounts(header,
//                    existingLine.getTotalAmount().negate(),
//                    existingLine.getTaxAmount().negate(),
//                    existingLine.getExcludeTaxAmount().negate());
//        }
//        invoiceApplyLineRepository.batchDeleteByPrimaryKey(invoiceApplyLines);
//
//        // Update headers in batch
//        invoiceApplyHeaderRepository
//                .batchUpdateByPrimaryKeySelective(new ArrayList<>(headerMap.values()));
//    }
//
//    @Override
//    public List<InvoiceApplyLine> selectByInvoiceHeader(Long headerId) {
//        InvoiceApplyLine invoiceApplyLine = new InvoiceApplyLine();
//        invoiceApplyLine.setApplyHeaderId(headerId);
//        return invoiceApplyLineRepository.selectList(invoiceApplyLine);
//    }
//
//    @Override
//    public List<InvoiceApplyLine> selectList(InvoiceApplyLine invoiceApplyLine) {
//        return invoiceApplyLineRepository.selectList(invoiceApplyLine);
//    }
//
//    @Override
//    public List<InvoiceApplyLine> selectAll() {
//        return invoiceApplyLineRepository.selectAll();
//    }
//
//    @Override
//    public List<InvoiceApplyLine> exportData(InvoiceApplyLine invoiceApplyLine) {
//        return invoiceApplyLineRepository.selectList(invoiceApplyLine);
//    }
    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

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
        // Retrieve all necessary headers and lines once at the beginning
        Map<Long, InvoiceApplyHeader> headerCache = new HashMap<>();
        Map<Long, InvoiceApplyLine> lineMap = invoiceApplyLineRepository.selectAll().stream()
                .collect(Collectors.toMap(InvoiceApplyLine::getApplyLineId, line -> line));

        List<InvApplyHeaderDTO> headerDTOS = invoiceApplyHeaderRepository.selectList(new InvApplyHeaderDTO());
        Map<Long, InvApplyHeaderDTO> headerMap = headerDTOS.stream()
                .collect(Collectors.toMap(InvApplyHeaderDTO::getApplyHeaderId, header -> header));

        // Prepare lists for insert and update
        List<InvoiceApplyLine> insertList = new ArrayList<>();
        List<InvoiceApplyLine> updateList = new ArrayList<>();

        // Process each line
        for (InvoiceApplyLine line : invoiceApplyLines) {
            calculateLineAmounts(line);

            // Ensure the header exists for the line
            if (line.getApplyHeaderId() == null) {
                throw new CommonException(Constants.MESSAGE_ERROR_HEADER_ID_CANNOT_BE_NULL);
            }

            InvApplyHeaderDTO headerDTO = headerMap.get(line.getApplyHeaderId());
            if (headerDTO == null) {
                throw new CommonException(Constants.MESSAGE_ERROR_NOT_FOUND);
            }

            // Use cache to retrieve or set the header
            InvoiceApplyHeader header = headerCache.computeIfAbsent(line.getApplyHeaderId(), id -> headerDTO);

            if (line.getApplyLineId() == null) {
                // New line: Insert operation
                updateHeaderAmounts(header, line.getTotalAmount(), line.getTaxAmount(), line.getExcludeTaxAmount());
                insertList.add(line);
            } else {
                // Existing line: Update operation
                InvoiceApplyLine existingLine = lineMap.get(line.getApplyLineId());
                if (existingLine == null) {
                    throw new CommonException(Constants.MESSAGE_ERROR_INV_LINE_NOT_FOUND);
                }

                updateHeaderAmounts(header,
                        line.getTotalAmount().subtract(existingLine.getTotalAmount()),
                        line.getTaxAmount().subtract(existingLine.getTaxAmount()),
                        line.getExcludeTaxAmount().subtract(existingLine.getExcludeTaxAmount()));
                updateList.add(line);
            }

            // Ensure header is updated in the cache
            headerCache.put(line.getApplyHeaderId(), header);
        }

        // Persist changes in bulk
        if (!insertList.isEmpty()) {
            invoiceApplyLineRepository.batchInsertSelective(insertList);
        }
        if (!updateList.isEmpty()) {
            invoiceApplyLineRepository.batchUpdateByPrimaryKeySelective(updateList);
        }

        // Batch update headers at the end
        List<InvoiceApplyHeader> headerUpdates = new ArrayList<>(headerCache.values());
        if (!headerUpdates.isEmpty()) {
            invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(headerUpdates);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeData(List<InvoiceApplyLine> invoiceApplyLines) {
        // Retrieve necessary headers and lines once
        List<InvApplyHeaderDTO> headerDTOS = invoiceApplyHeaderRepository
                .selectList(new InvApplyHeaderDTO());
        Map<Long, InvApplyHeaderDTO> headerMap = headerDTOS.stream()
                .collect(Collectors.toMap(InvApplyHeaderDTO::getApplyHeaderId, header -> header));

        List<InvoiceApplyLine> invoiceApplyLines1 = invoiceApplyLineRepository.selectAll();
        Map<Long, InvoiceApplyLine> lineMap = invoiceApplyLines1.stream()
                .collect(Collectors.toMap(InvoiceApplyLine::getApplyLineId, line -> line));

        // Process each line to update header amounts
        for (InvoiceApplyLine line : invoiceApplyLines) {
            InvoiceApplyLine existingLine = lineMap.get(line.getApplyLineId());
            if (existingLine == null) {
                throw new CommonException(Constants.MESSAGE_ERROR_INV_LINE_NOT_FOUND);
            }

            InvoiceApplyHeader header = headerMap.get(line.getApplyHeaderId());

            updateHeaderAmounts(header,
                    existingLine.getTotalAmount().negate(),
                    existingLine.getTaxAmount().negate(),
                    existingLine.getExcludeTaxAmount().negate());
        }

        // Batch delete lines and update headers
        invoiceApplyLineRepository.batchDeleteByPrimaryKey(invoiceApplyLines);
        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(new ArrayList<>(headerMap.values()));
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
}

