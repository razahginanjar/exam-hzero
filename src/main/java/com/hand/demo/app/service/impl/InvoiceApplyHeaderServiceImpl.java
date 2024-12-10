package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.cache.ProcessCacheValue;
import org.hzero.core.redis.RedisHelper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.json.stream.JsonParsingException;
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
                                              InvApplyHeaderDTO invoiceApplyHeader) {
        return PageHelper
                .doPageAndSort(pageRequest,
                        () -> invoiceApplyHeaderRepository.selectList(invoiceApplyHeader));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @ProcessCacheValue
    public void saveData(List<InvApplyHeaderDTO> invoiceApplyHeaders) {
        validateLovData(invoiceApplyHeaders);
        for (InvApplyHeaderDTO invoiceApplyHeader : invoiceApplyHeaders) {
            if(Objects.isNull(invoiceApplyHeader.getTenantId()))
            {
                invoiceApplyHeader.setTenantId(BaseConstants.DEFAULT_TENANT_ID);
            }
        }

        List<InvApplyHeaderDTO> insertList = invoiceApplyHeaders.stream()
                .filter(header -> header.getApplyHeaderId() == null && header.getApplyHeaderNumber() == null)
                .collect(Collectors.toList());

        List<InvApplyHeaderDTO> updateList = invoiceApplyHeaders.stream()
                .filter(header -> header.getApplyHeaderId() != null || header.getApplyHeaderNumber() != null)
                .collect(Collectors.toList());

        List<InvApplyHeaderDTO> headerDTOS1 = processInsertHeaders(insertList);
        List<InvApplyHeaderDTO> headerDTOS = processUpdateHeaders(updateList);
        Map<String, InvApplyHeaderDTO> mapHeader = new HashMap<>();

        if(Objects.nonNull(headerDTOS1) && CollectionUtils.isNotEmpty(headerDTOS1))
        {
            for (InvApplyHeaderDTO invApplyHeaderDTO : headerDTOS1) {
                mapHeader.put(invApplyHeaderDTO.getApplyHeaderNumber(), invApplyHeaderDTO);
            }
        }


        if(Objects.nonNull(headerDTOS) && CollectionUtils.isNotEmpty(headerDTOS))
        {
            for (InvApplyHeaderDTO invApplyHeaderDTO : headerDTOS) {
                mapHeader.put(invApplyHeaderDTO.getApplyHeaderNumber(), invApplyHeaderDTO);
            }
        }

        for (InvApplyHeaderDTO invoiceApplyHeader : invoiceApplyHeaders) {
            InvApplyHeaderDTO invApplyHeaderDTO = mapHeader.get(invoiceApplyHeader.getApplyHeaderNumber());
            invoiceApplyHeader.setApplyHeaderId(invApplyHeaderDTO.getApplyHeaderId());
        }

        processInvoiceLines(invoiceApplyHeaders);
        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineService.selectAll();

        if(Objects.nonNull(headerDTOS) && CollectionUtils.isNotEmpty(headerDTOS))
        {
            Map<Long, List<InvoiceApplyLine>> lineMap = invoiceApplyLines.stream()
                    .collect(Collectors.groupingBy(InvoiceApplyLine::getApplyHeaderId));
            for (InvoiceApplyHeader invoiceApplyHeader : headerDTOS)
            {
                InvApplyHeaderDTO invApplyHeaderDTO = mapToInvApplyHeaderDTO(invoiceApplyHeader);
                invApplyHeaderDTO.setInvoiceApplyLines(lineMap.get(invApplyHeaderDTO.getApplyHeaderId()));
                cacheHeaderDetails(invApplyHeaderDTO.getApplyHeaderId(), invApplyHeaderDTO);
            }
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<InvApplyHeaderDTO> invoiceApplyHeaders) {
        List<InvApplyHeaderDTO> invoiceApplyHeaders1
                = invoiceApplyHeaderRepository.selectList(new InvApplyHeaderDTO());
        Map<Long, InvApplyHeaderDTO> headerDTOMap = new HashMap<>();
        for (InvApplyHeaderDTO invApplyHeaderDTO : invoiceApplyHeaders1) {
            headerDTOMap.put(invApplyHeaderDTO.getApplyHeaderId(), invApplyHeaderDTO);
        }

        for (InvApplyHeaderDTO invoiceApplyHeader : invoiceApplyHeaders) {
            boolean b = headerDTOMap.containsKey(invoiceApplyHeader.getApplyHeaderId());
            if(!b)
            {
                throw new CommonException(Constants.MESSAGE_ERROR_NOT_FOUND, invoiceApplyHeader);
            }
            invoiceApplyHeader.setDelFlag(1);
        }
        List<InvoiceApplyHeader> collect = invoiceApplyHeaders.stream()
                .map(invApplyHeaderDTO -> {
                    InvoiceApplyHeader invoiceApplyHeader = new InvoiceApplyHeader();
                    BeanUtils.copyProperties(invApplyHeaderDTO, invoiceApplyHeader);
                    return invoiceApplyHeader;
                })
                .collect(Collectors.toList());
        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(collect);
    }

    @Override
    public InvoiceApplyHeader selectOne(Long id) {
        return invoiceApplyHeaderRepository.selectByPrimary(id);
    }

    @Override
    @Transactional(readOnly = true)
    @ProcessCacheValue
    public InvApplyHeaderDTO selectDetail(Long invHeaderId, Long tenantId) {
        String s = redisHelper.strGet(Constants.CACHE_KEY_PREFIX + ":" + invHeaderId);
        try{
            if(Objects.nonNull(s))
            {
                return objectMapper.readValue(s, InvApplyHeaderDTO.class);
            }
        }catch (JsonProcessingException e)
        {
            log.error(e.getMessage());
        }

        InvApplyHeaderDTO header = invoiceApplyHeaderRepository.selectByPrimary(invHeaderId);
        if (header == null) {
            throw new CommonException(Constants.MESSAGE_ERROR_NOT_FOUND, invHeaderId);
        }
        List<InvoiceApplyLine> invoiceApplyLines =
                invoiceApplyLineService.selectByInvoiceHeader(invHeaderId);
        header.setInvoiceApplyLines(invoiceApplyLines);
        cacheHeaderDetails(invHeaderId, header);
        return header;
    }

    @Override
    public InvoiceApplyHeader selectDetailSelective(InvoiceApplyHeader invoiceApplyHeader) {
        return invoiceApplyHeaderRepository.selectOne(invoiceApplyHeader);
    }

    @Override
    @ProcessLovValue
    @Transactional(readOnly = true)
    public List<InvApplyHeaderDTO> exportData(InvApplyHeaderDTO invoiceApplyHeader) {
        // Fetch LOV mappings for tenant
        Map<String, Map<String, String>> stringMapMap =
                fetchLovMaps(invoiceApplyHeader.getTenantId());

        // Fetch headers based on the filter criteria
        List<InvApplyHeaderDTO> headerDTOS =
                invoiceApplyHeaderRepository.selectList(invoiceApplyHeader);

        // Fetch lines for all headers in a single batch call (if supported, else loop as in original code)
        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineService.selectAll();

        Map<Long, List<InvoiceApplyLine>> lineMap = invoiceApplyLines.stream()
                .collect(Collectors.groupingBy(InvoiceApplyLine::getApplyHeaderId));

        // Transform headers into DTOs
        return headerDTOS.stream()
                .peek(header -> {
                    // Set lines
                    header.setInvoiceApplyLines(lineMap.get(header.getApplyHeaderId()));
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
        Long defaultTenantId = BaseConstants.DEFAULT_TENANT_ID;
        Map<String, Map<String, String>> lovMaps = fetchLovMaps(defaultTenantId);
        headers.forEach(invApplyHeaderDTO ->
                validateLovDataForHeader(lovMaps, invApplyHeaderDTO));
    }

    private void validateLovDataForHeader(Map<String, Map<String, String>> lovMaps,
                                          InvoiceApplyHeader header) {
        String color = lovMaps.get(Constants.LOV_CODE_COLOR).get(header.getInvoiceColor());
        String status = lovMaps.get(Constants.LOV_CODE_STATUS).get(header.getApplyStatus());
        String type = lovMaps.get(Constants.LOV_CODE_TYPE).get(header.getInvoiceType());

        if (color == null || status == null || type == null) {
            throw new CommonException(Constants.MESSAGE_ERROR_INVALID_LOV);
        }
    }


    private void processInvoiceLines(List<InvApplyHeaderDTO> headers) {
//        log.info("get in the function");
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

    private List<InvApplyHeaderDTO> processInsertHeaders(List<InvApplyHeaderDTO> headers) {
        if (headers.isEmpty()) return null;

        // Prepare the headers
        headers.forEach(header -> {
            header.setApplyHeaderNumber(codeRuleBuilder.generateCode(Constants.CODE_RULE_HEADER, new HashMap<>()));
            header.setSubmitTime(Optional.ofNullable(header.getSubmitTime()).orElse(Date.from(Instant.now())));
            cacheHeaderDetails(header.getApplyHeaderId(), header);
        });

        // Map DTOs to Entities
        List<InvoiceApplyHeader> entities = headers.stream()
                .map(this::mapToInvoiceApplyHeader) // Mapping method
                .collect(Collectors.toList());

        // Perform batch insert
        List<InvoiceApplyHeader> invoiceApplyHeaders = invoiceApplyHeaderRepository.batchInsertSelective(entities);
        return invoiceApplyHeaders.stream().map(this::mapToInvApplyHeaderDTO).collect(Collectors.toList());
    }

    // Mapping method
    private InvoiceApplyHeader mapToInvoiceApplyHeader(InvApplyHeaderDTO dto) {
        InvoiceApplyHeader entity = new InvoiceApplyHeader();
        BeanUtils.copyProperties(dto, entity); // Or map manually if needed
        return entity;
    }

    private InvApplyHeaderDTO mapToInvApplyHeaderDTO(InvoiceApplyHeader entity) {
        InvApplyHeaderDTO dto = new InvApplyHeaderDTO();
        BeanUtils.copyProperties(entity, dto); // Or map manually if needed
        return dto;
    }

    private List<InvApplyHeaderDTO> processUpdateHeaders(List<InvApplyHeaderDTO> headers) {
        if (headers.isEmpty()) return null;

        // Fetch all InvoiceApplyLines
        List<InvoiceApplyLine> invoiceApplyLines = invoiceApplyLineService.selectAll();
        List<InvoiceApplyHeader> invoiceApplyHeaders = invoiceApplyHeaderRepository.selectAll();

        // Create a map to group InvoiceApplyLines by ApplyHeaderId
        Map<Long, List<InvoiceApplyLine>> mapping = new HashMap<>();
        Map<Long, InvoiceApplyHeader> mapHeader = new HashMap<>();

        // Group InvoiceApplyLines by ApplyHeaderId
        for (InvoiceApplyLine invoiceApplyLine : invoiceApplyLines) {
            mapping.computeIfAbsent(invoiceApplyLine.getApplyHeaderId(),
                    k -> new ArrayList<>()).add(invoiceApplyLine);
        }

        for (InvoiceApplyHeader invoiceApplyHeader : invoiceApplyHeaders) {
            mapHeader.put(invoiceApplyHeader.getApplyHeaderId(), invoiceApplyHeader);
        }
        // Process each header
        for (InvApplyHeaderDTO header : headers) {
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
            cacheHeaderDetails(header.getApplyHeaderId(), header);
        }

        // Map DTOs to Entities
        List<InvoiceApplyHeader> entities = headers.stream()
                .map(this::mapToInvoiceApplyHeader) // Mapping method
                .collect(Collectors.toList());
        // Perform the batch update
        List<InvoiceApplyHeader> invoiceApplyHeaders1 = invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(entities);
        return invoiceApplyHeaders1.stream().map(this::mapToInvApplyHeaderDTO).collect(Collectors.toList());
    }

}

