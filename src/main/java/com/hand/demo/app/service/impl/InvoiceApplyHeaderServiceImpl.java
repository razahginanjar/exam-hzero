package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.api.dto.InvApplyHeaderDTO;
import com.hand.demo.app.service.InvoiceApplyLineService;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
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
import org.opensaml.ws.wstrust.impl.CodeBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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

    private static final String CACHE_KEY_PREFIX = "hexam-47837:invoice-header";

    @Override
    public Page<InvApplyHeaderDTO> selectList(PageRequest pageRequest,
                                              InvoiceApplyHeader invoiceApplyHeader) {
        PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize());
        List<InvoiceApplyHeader> invoiceApplyHeaders = invoiceApplyHeaderRepository.selectList(invoiceApplyHeader);

        List<InvApplyHeaderDTO> dtoList = invoiceApplyHeaders.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return new Page<>(dtoList, new PageInfo(pageRequest.getPage(), pageRequest.getSize()), invoiceApplyHeaders.size());

    }

    @Override
    public void saveData(List<InvoiceApplyHeader> invoiceApplyHeaders) {
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
        cacheHeaderDetails(invHeaderId, dto);
        return dto;
    }

    @Override
    public InvoiceApplyHeader selectDetailSelective(InvoiceApplyHeader invoiceApplyHeader) {
        return invoiceApplyHeaderRepository.selectOne(invoiceApplyHeader);
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
        return new ModelMapper().map(header, InvApplyHeaderDTO.class);
    }


    private void cacheHeaderDetails(Long invHeaderId, InvApplyHeaderDTO dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            redisHelper.hshPut(CACHE_KEY_PREFIX, String.valueOf(invHeaderId), json);
            redisHelper.setExpire(CACHE_KEY_PREFIX + "::" + invHeaderId, 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("Failed to cache header details for ID {}: {}", invHeaderId, e.getMessage(), e);
        }
    }


    private void validateLovData(List<InvoiceApplyHeader> headers) {
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


    private void processInvoiceLines(List<InvoiceApplyHeader> headers) {
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

        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(headers);
    }

}
