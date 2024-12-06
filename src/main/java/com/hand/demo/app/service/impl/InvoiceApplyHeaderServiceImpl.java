package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.api.dto.InvApplyHeaderDTO;
import com.hand.demo.app.service.InvoiceApplyLineService;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.infra.constant.Constants;
import io.choerodon.core.domain.Page;
import io.choerodon.core.domain.PageInfo;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.cache.ProcessCacheValue;
import org.hzero.core.redis.RedisHelper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Invoice Apply Header Table(InvoiceApplyHeader)应用服务
 *
 * @author razah
 * @since 2024-12-03 09:28:06
 */
@Slf4j
@Service
public class InvoiceApplyHeaderServiceImpl implements InvoiceApplyHeaderService {
    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    private LovAdapter lovAdapter;

    @Autowired
    private RedisHelper redisHelper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CodeRuleBuilder codeRuleBuilder;

    @Autowired
    private InvoiceApplyLineService invoiceApplyLineService;


    @Override
    public Page<InvApplyHeaderDTO> selectList(PageRequest pageRequest,
                                              InvoiceApplyHeader invoiceApplyHeader) {
        PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize());
        List<InvoiceApplyHeader> invoiceApplyHeaders =
                invoiceApplyHeaderRepository.selectList(invoiceApplyHeader);

        List<InvApplyHeaderDTO> dtoList = invoiceApplyHeaders.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return new Page<>(dtoList, new PageInfo(pageRequest.getPage(), pageRequest.getSize()), invoiceApplyHeaders.size());

    }

    @Override
    public void saveData(List<InvApplyHeaderDTO> invoiceApplyHeaders) {
        validateLovData(invoiceApplyHeaders);

        List<InvoiceApplyHeader> insertList = invoiceApplyHeaders.stream()
                .filter(header -> header.getApplyHeaderId() == null && header.getApplyHeaderNumber() == null)
                .collect(Collectors.toList());

        List<InvoiceApplyHeader> updateList = invoiceApplyHeaders.stream()
                .filter(header -> header.getApplyHeaderId() != null || header.getApplyHeaderNumber() != null)
                .collect(Collectors.toList());

        processInsertHeaders(insertList);
        processUpdateHeaders(updateList);

        processInvoiceLines(invoiceApplyHeaders);
    }

    @Override
    public void delete(List<InvoiceApplyHeader> invoiceApplyHeaders) {
        for (InvoiceApplyHeader invoiceApplyHeader : invoiceApplyHeaders) {
            InvoiceApplyHeader invoiceApplyHeader1 =
                    invoiceApplyHeaderRepository.selectOne(invoiceApplyHeader);
            if(invoiceApplyHeader1 == null){
                throw new CommonException(Constants.MESSAGE_ERROR_NOT_FOUND, invoiceApplyHeader);
            }
            invoiceApplyHeader1.setDelFlag(1);
            invoiceApplyHeaderRepository.updateByPrimaryKeySelective(invoiceApplyHeader1);
        }
    }

    @Override
    public InvoiceApplyHeader selectOne(Long id) {
        return invoiceApplyHeaderRepository.selectByPrimary(id);
    }

    @Override
    @ProcessCacheValue
    public InvApplyHeaderDTO selectDetail(Long invHeaderId, Long tenantId) {
        InvoiceApplyHeader invoiceApplyHeader = new InvoiceApplyHeader();
        invoiceApplyHeader.setApplyHeaderId(invHeaderId);
        invoiceApplyHeader.setTenantId(tenantId);
        InvoiceApplyHeader header = invoiceApplyHeaderRepository
                .selectOne(invoiceApplyHeader);
        if (header == null) {
            throw new CommonException(Constants.MESSAGE_ERROR_NOT_FOUND, invHeaderId);
        }
        InvApplyHeaderDTO dto = new ModelMapper().map(header, InvApplyHeaderDTO.class);
        List<InvoiceApplyLine> invoiceApplyLines =
                invoiceApplyLineService.selectByInvoiceHeader(invHeaderId);
        dto.setInvoiceApplyLines(invoiceApplyLines);
        cacheHeaderDetails(invHeaderId, dto);
        return dto;
    }

    @Override
    public InvoiceApplyHeader selectDetailSelective(InvoiceApplyHeader invoiceApplyHeader) {
        return invoiceApplyHeaderRepository.selectOne(invoiceApplyHeader);
    }

    @Override
    public List<InvApplyHeaderDTO> exportData(InvoiceApplyHeader invoiceApplyHeader) {
        // Fetch LOV mappings for tenant
        Map<String, Map<String, String>> stringMapMap =
                fetchLovMaps(invoiceApplyHeader.getTenantId());

        // Fetch headers based on the filter criteria
        List<InvoiceApplyHeader> invoiceApplyHeaders =
                invoiceApplyHeaderRepository.selectList(invoiceApplyHeader);

        // Collect IDs of headers for fetching lines
        List<Long> headerIds = new ArrayList<>();
        for (InvoiceApplyHeader applyHeader : invoiceApplyHeaders) {
            headerIds.add(applyHeader.getApplyHeaderId());
        }

        // Fetch lines for all headers in a single batch call (if supported, else loop as in original code)
        Map<Long, List<InvoiceApplyLine>> lineMap = headerIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> {
                            InvoiceApplyLine query = new InvoiceApplyLine();
                            query.setApplyHeaderId(id);
                            return invoiceApplyLineService.selectList(query);
                        }
                ));

        // Transform headers into DTOs
        ModelMapper modelMapper = new ModelMapper();
        return invoiceApplyHeaders.stream()
                .map(header -> {
                    // Map header to DTO
                    InvApplyHeaderDTO dto = modelMapper.map(header, InvApplyHeaderDTO.class);

                    // Set LOV meanings
                    dto.setInvoiceColorMeaning(stringMapMap.getOrDefault(Constants.LOV_CODE_COLOR, Collections.emptyMap())
                            .get(dto.getInvoiceColor()));
                    dto.setApplyStatusMeaning(stringMapMap.getOrDefault(Constants.LOV_CODE_STATUS, Collections.emptyMap())
                            .get(dto.getApplyStatus()));
                    dto.setInvoiceTypeMeaning(stringMapMap.getOrDefault(Constants.LOV_CODE_TYPE, Collections.emptyMap())
                            .get(dto.getInvoiceType()));

                    // Set lines
                    dto.setInvoiceApplyLines(lineMap.get(header.getApplyHeaderId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }


    private Map<String, Map<String, String>> fetchLovMaps(Long tenantId) {
        Map<String, Map<String, String>> lovMaps = new HashMap<>();

        lovMaps.put(Constants.LOV_CODE_COLOR, lovAdapter.queryLovValue(Constants.LOV_CODE_COLOR, tenantId)
                .stream().collect(Collectors.toMap(LovValueDTO::getValue, LovValueDTO::getMeaning)));

        lovMaps.put(Constants.LOV_CODE_STATUS, lovAdapter.queryLovValue(Constants.LOV_CODE_STATUS, tenantId)
                .stream().collect(Collectors.toMap(LovValueDTO::getValue, LovValueDTO::getMeaning)));

        lovMaps.put(Constants.LOV_CODE_TYPE, lovAdapter.queryLovValue(Constants.LOV_CODE_TYPE, tenantId)
                .stream().collect(Collectors.toMap(LovValueDTO::getValue, LovValueDTO::getMeaning)));

        return lovMaps;
    }


    private InvApplyHeaderDTO mapToDTO(InvoiceApplyHeader header) {
        InvApplyHeaderDTO map = new ModelMapper().map(header, InvApplyHeaderDTO.class);
        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineService.selectByInvoiceHeader(header.getApplyHeaderId());
        map.setInvoiceApplyLines(invoiceApplyLines);
        return map;
    }


    private void cacheHeaderDetails(Long invHeaderId, InvApplyHeaderDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            redisHelper.strSet(Constants.CACHE_KEY_PREFIX+":"+invHeaderId,
                    json, 2, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("Failed to cache header details for ID {}: {}", invHeaderId, e.getMessage(), e);
        }
    }


    private void validateLovData(List<InvApplyHeaderDTO> headers) {
        headers.forEach(this::validateLovDataForHeader);
    }

    private void validateLovDataForHeader(InvoiceApplyHeader header) {
        Map<String, Map<String, String>> lovMaps = fetchLovMaps(header.getTenantId());
        String color = lovMaps.get(Constants.LOV_CODE_COLOR).get(header.getInvoiceColor());
        String status = lovMaps.get(Constants.LOV_CODE_STATUS).get(header.getApplyStatus());
        String type = lovMaps.get(Constants.LOV_CODE_TYPE).get(header.getInvoiceType());

        if (color == null || status == null || type == null) {
            throw new CommonException(Constants.MESSAGE_ERROR_INVALID_LOV);
        }
    }


    private void processInvoiceLines(List<InvApplyHeaderDTO> headers) {
        headers.forEach(header -> {
            if (!CollectionUtils.isEmpty(header.getInvoiceApplyLines())) {
                header.getInvoiceApplyLines().forEach(line -> {
                    line.setApplyHeaderId(header.getApplyHeaderId());
                    line.setTenantId(header.getTenantId());
                });
                invoiceApplyLineService.saveData(header.getInvoiceApplyLines());
            }
        });
    }

    private void processInsertHeaders(List<InvoiceApplyHeader> headers) {
        if (headers.isEmpty()) return;

        headers.forEach(header -> {
            header.setApplyHeaderNumber(codeRuleBuilder.generateCode(Constants.CODE_RULE_HEADER, new HashMap<>()));
            header.setSubmitTime(Optional.ofNullable(header.getSubmitTime()).orElse(Date.from(Instant.now())));
        });
        invoiceApplyHeaderRepository.batchInsertSelective(headers);
    }

    private void processUpdateHeaders(List<InvoiceApplyHeader> headers) {
        if (headers.isEmpty()) return;

        // Fetch all InvoiceApplyLines
        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineService.selectAll();
        List<InvoiceApplyHeader> invoiceApplyHeaders = invoiceApplyHeaderRepository.selectAll();
        // Create a map to group InvoiceApplyLines by ApplyHeaderId
        Map<Long, List<InvoiceApplyLine>> mapping = new HashMap<>();
        Map<Long, InvoiceApplyHeader> mapHeader = new HashMap<>();

        // Group InvoiceApplyLines by ApplyHeaderId
        for (InvoiceApplyLine invoiceApplyLine : invoiceApplyLines) {
            mapping.computeIfAbsent(invoiceApplyLine.getApplyHeaderId(), k -> new ArrayList<>()).add(invoiceApplyLine);
        }

        for (InvoiceApplyHeader invoiceApplyHeader : invoiceApplyHeaders) {
            mapHeader.put(invoiceApplyHeader.getApplyHeaderId(), invoiceApplyHeader);
        }
        // Process each header
        for (InvoiceApplyHeader header : headers) {
            List<InvoiceApplyLine> invoiceApplyLines1 = mapping.get(header.getApplyHeaderId());
            if (invoiceApplyLines1 == null) continue;  // Skip if no lines exist for this header

            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal excludeTaxAmount = BigDecimal.ZERO;
            BigDecimal taxAmount = BigDecimal.ZERO;

            // Accumulate total amounts, exclude tax amounts, and tax amounts
            for (InvoiceApplyLine line : invoiceApplyLines1) {
                totalAmount = totalAmount.add(line.getTotalAmount());
                excludeTaxAmount = excludeTaxAmount.add(line.getExcludeTaxAmount());  // Assuming there's an excludeTaxAmount field
                taxAmount = taxAmount.add(line.getTaxAmount());
            }

            // Set the calculated amounts to the header
            header.setTotalAmount(totalAmount);
            header.setExcludeTaxAmount(excludeTaxAmount);  // Set the exclude tax amount directly
            header.setTaxAmount(taxAmount);
            header.setApplyHeaderNumber(mapHeader.get(header.getApplyHeaderId()).getApplyHeaderNumber());
        }

        // Perform the batch update
        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(headers);
    }

}

