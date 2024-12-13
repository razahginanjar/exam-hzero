package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.api.dto.FeignIamSelfDTO;
import com.hand.demo.api.dto.InvApplyHeaderDTO;
import com.hand.demo.api.dto.ReportExportDTO;
import com.hand.demo.app.service.InvoiceApplyLineService;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.infra.constant.Constants;
import com.hand.demo.infra.feign.IamFeign;
import io.choerodon.core.domain.Page;
import io.choerodon.core.domain.PageInfo;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.hzero.boot.apaas.common.userinfo.infra.feign.IamRemoteService;
import org.hzero.boot.interfaces.infra.feign.HiamRemoteFeignClient;
import org.hzero.boot.interfaces.sdk.dto.UserVO;
import org.hzero.boot.message.feign.PlatformRemoteService;
import org.hzero.boot.platform.code.builder.CodeRuleBuilder;
import org.hzero.boot.platform.lov.adapter.LovAdapter;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.boot.platform.lov.dto.LovValueDTO;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.cache.ProcessCacheValue;
import org.hzero.core.redis.RedisHelper;
import org.hzero.mybatis.domian.Condition;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    private IamRemoteService iamRemoteService;


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

        List<InvApplyHeaderDTO> insertList = invoiceApplyHeaders.stream()
                .filter(header -> header.getApplyHeaderId() == null && header.getApplyHeaderNumber() == null)
                .collect(Collectors.toList());

        List<InvApplyHeaderDTO> updateList = invoiceApplyHeaders.stream()
                .filter(header -> header.getApplyHeaderId() != null || header.getApplyHeaderNumber() != null)
                .collect(Collectors.toList());

        processInsertHeaders(insertList);
        processUpdateHeaders(updateList);
        processInvoiceLines(invoiceApplyHeaders);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<InvApplyHeaderDTO> invoiceApplyHeaders) {
//        List<InvApplyHeaderDTO> invoiceApplyHeaders1
//                = invoiceApplyHeaderRepository.selectList(new InvApplyHeaderDTO());

        Set<String> headerIds = invoiceApplyHeaders.stream()
                .map(header -> header.getApplyHeaderId().toString())
                .collect(Collectors.toSet());

        List<InvoiceApplyHeader> Headers =
                invoiceApplyHeaderRepository.selectByIds(String.join(",", headerIds));

//        List<InvoiceApplyHeader> invoiceApplyHeaders2
//                = invoiceApplyHeaderRepository.selectByIds(ids.toString());


        Map<Long, InvoiceApplyHeader> headerDTOMap = new HashMap<>();
        for (InvoiceApplyHeader invApplyHeaderDTO : Headers) {
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
//        List<InvoiceApplyHeader> collect = invoiceApplyHeaders.stream()
//                .map(invApplyHeaderDTO -> {
//                    InvoiceApplyHeader invoiceApplyHeader = new InvoiceApplyHeader();
//                    BeanUtils.copyProperties(invApplyHeaderDTO, invoiceApplyHeader);
//                    return invoiceApplyHeader;
//                })
//                .collect(Collectors.toList());
        invoiceApplyHeaderRepository.batchUpdateByPrimaryKeySelective(new ArrayList<>(invoiceApplyHeaders));
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
        header.setRealName(DetailsHelper.getUserDetails().getRealName());
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
//        Map<String, Map<String, String>> stringMapMap =
//                fetchLovMaps(invoiceApplyHeader.getTenantId());

        // Fetch headers based on the filter criteria
        List<InvApplyHeaderDTO> headerDTOS =
                invoiceApplyHeaderRepository.selectList(invoiceApplyHeader);

        // Fetch lines for all headers in a single batch call (if supported,
        // else loop as in original code)
        List<Long> idsHeader = new ArrayList<>();
        headerDTOS.forEach(header -> {idsHeader.add(header.getApplyHeaderId());});
        List<InvoiceApplyLine> fromHeaders = invoiceApplyLineService.getFromHeaders(idsHeader);
//        List<InvoiceApplyLine> invoiceApplyLines
//                = invoiceApplyLineService.selectAll();

        Map<Long, List<InvoiceApplyLine>> lineMap = fromHeaders.stream()
                .collect(Collectors.groupingBy(InvoiceApplyLine::getApplyHeaderId));

        // Transform headers into DTOs
        return headerDTOS.stream()
                .peek(header -> {
                    // Set lines
                    header.setInvoiceApplyLines(lineMap.get(header.getApplyHeaderId()));
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<InvoiceApplyHeader> selectByHeaderIds(List<Long> ids) {
        Set<String> headerIds = ids.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());

        return invoiceApplyHeaderRepository.selectByIds(String.join(",", headerIds));
    }

    @Override
    @ProcessLovValue
    public ReportExportDTO selectReport(Long organizationId, ReportExportDTO reportExportDTO) {
        Condition condition = new Condition(InvoiceApplyHeader.class);
        condition.createCriteria().andEqualTo(InvoiceApplyHeader.FIELD_TENANT_ID,
                organizationId
                );
        if(reportExportDTO.getApplyNumberFrom() != null && reportExportDTO.getApplyNumberTo() != null) {

            condition.and().andBetween(
                    InvoiceApplyHeader.FIELD_APPLY_HEADER_NUMBER,
                    reportExportDTO.getApplyNumberFrom(),
                    reportExportDTO.getApplyNumberTo()
            );
        }
        if(reportExportDTO.getCreatedDateFrom() != null && reportExportDTO.getCreatedDateTo() != null) {
            condition.and().andBetween(
                    InvoiceApplyHeader.FIELD_CREATION_DATE,
                    reportExportDTO.getCreatedDateFrom(),
                    reportExportDTO.getCreatedDateTo()
            );
        }
        if(reportExportDTO.getSubmitTimeFrom()!= null && reportExportDTO.getSubmitTimeTo() != null) {
            condition.and().andBetween(
                    InvoiceApplyHeader.FIELD_SUBMIT_TIME,
                    reportExportDTO.getSubmitTimeFrom(),
                    reportExportDTO.getSubmitTimeTo()
            );
        }
        Map<String, Map<String, String>> stringMapMap = fetchLovMaps(0L);

        if(reportExportDTO.getInvoiceType() != null) {
            String s = stringMapMap.get(Constants.LOV_CODE_TYPE).get(reportExportDTO.getInvoiceType());
            if(s.isEmpty())
            {
                throw new CommonException(Constants.MESSAGE_ERROR_INVALID_LOV);
            }
            condition.and().andEqualTo(InvoiceApplyHeader.FIELD_INVOICE_TYPE, reportExportDTO.getInvoiceType());
        }
        if(!reportExportDTO.getApplyStatuses().isEmpty()) {
            for (String applyStatus : reportExportDTO.getApplyStatuses()) {
                String s = stringMapMap.get(Constants.LOV_CODE_STATUS).get(applyStatus);
                if(s.isEmpty())
                {
                    throw new CommonException(Constants.MESSAGE_ERROR_INVALID_LOV);
                }
            }
            condition.and().andIn(InvoiceApplyHeader.FIELD_APPLY_STATUS, reportExportDTO.getApplyStatuses());
        }
        try{
            ResponseEntity<String> stringResponseEntity = iamRemoteService.selectSelf();
            UserVO userVO = objectMapper.readValue(stringResponseEntity.getBody(), UserVO.class);
            reportExportDTO.setTenantName(userVO.getTenantName());
        } catch (JsonProcessingException e) {
            throw new CommonException(e.getMessage());
        }
        List<InvoiceApplyHeader> invoiceApplyHeaders = invoiceApplyHeaderRepository
                .selectByCondition(condition);
        List<InvApplyHeaderDTO> headersDTOs = invoiceApplyHeaders.stream()
                .map(header -> {
                    InvApplyHeaderDTO dto = new InvApplyHeaderDTO();
                    BeanUtils.copyProperties(header, dto);
                    return dto;
                })
                .collect(Collectors.toList());

        //set ids header to Set<>
        List<Long> headerIds = new ArrayList<>();
        for (InvoiceApplyHeader invoiceApplyHeader : invoiceApplyHeaders) {
            headerIds.add(invoiceApplyHeader.getApplyHeaderId());
        }
        List<InvoiceApplyLine> fromHeaders = invoiceApplyLineService.getFromHeaders(headerIds);
        Map<Long, List<InvoiceApplyLine>> lineMap = new HashMap<>();
        for (InvoiceApplyLine fromHeader : fromHeaders) {
            lineMap.computeIfAbsent(fromHeader.getApplyHeaderId(), k -> new ArrayList<>())
                    .add(fromHeader);
        }
        for (InvApplyHeaderDTO headersDTO : headersDTOs) {
//            headersDTO.setInvoiceApplyLines();
            List<String> invoiceNames = lineMap.get(headersDTO.getApplyHeaderId())
                    .stream()
                    .map(InvoiceApplyLine::getInvoiceName)
                    .collect(Collectors.toList());
            headersDTO.setInvoiceNames(String.join(", ", invoiceNames));
        }

        reportExportDTO.setHeaderDTOS(headersDTOs);
        return reportExportDTO;
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

    private void processInsertHeaders(List<InvApplyHeaderDTO> headers) {
        if (headers.isEmpty()) return;
        // Prepare the headers
        headers.forEach(header -> {
            header.setApplyHeaderNumber(codeRuleBuilder.generateCode(Constants.CODE_RULE_HEADER, new HashMap<>()));
            header.setSubmitTime(Optional.ofNullable(header.getSubmitTime()).orElse(Date.from(Instant.now())));
        });
        // Perform batch insert
        invoiceApplyHeaderRepository.batchInsertSelective(new ArrayList<>(headers));
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

    private void processUpdateHeaders(List<InvApplyHeaderDTO> headers) {
        if (headers.isEmpty()) return;
        List<InvoiceApplyHeader> invoiceApplyHeaders1 =
                selectByHeaderIds(headers.stream().map(InvoiceApplyHeader::getApplyHeaderId).collect(Collectors.toList()));
        Map<Long, InvoiceApplyHeader> headerMap = new HashMap<>();
        for (InvoiceApplyHeader invoiceApplyHeader : invoiceApplyHeaders1) {
            headerMap.put(invoiceApplyHeader.getApplyHeaderId(), invoiceApplyHeader);
        }
        List<InvApplyHeaderDTO> collect = headers.stream()
                .peek(header -> {
                    // Filter out fields that are null or undesired
                    if (header.getApplyStatus() == null)
                        header.setApplyStatus(headerMap.get(header.getApplyHeaderId()).getApplyStatus());
                    if (header.getBillToAddress() == null)
                        header.setBillToAddress(headerMap.get(header.getApplyHeaderId()).getBillToAddress());
                    if (header.getBillToEmail() == null)
                        header.setBillToEmail(headerMap.get(header.getApplyHeaderId()).getBillToEmail());
                    if (header.getBillToPerson() == null)
                        header.setBillToPerson(headerMap.get(header.getApplyHeaderId()).getBillToPerson());
                    if (header.getBillToPhone() == null)
                        header.setBillToPhone(headerMap.get(header.getApplyHeaderId()).getBillToPhone());
                    if (header.getInvoiceColor() == null)
                        header.setInvoiceColor(headerMap.get(header.getApplyHeaderId()).getInvoiceColor());
                    if (header.getInvoiceType() == null)
                        header.setInvoiceType(headerMap.get(header.getApplyHeaderId()).getInvoiceType());
                    if (header.getRemark() == null)
                        header.setRemark(headerMap.get(header.getApplyHeaderId()).getRemark());
                })
                .collect(Collectors.toList());

        invoiceApplyHeaderRepository.batchUpdateOptional(new ArrayList<>(headers),
                InvoiceApplyHeader.FIELD_APPLY_STATUS,
                InvoiceApplyHeader.FIELD_BILL_TO_ADDRESS,
                InvoiceApplyHeader.FIELD_BILL_TO_EMAIL,
                InvoiceApplyHeader.FIELD_BILL_TO_PERSON,
                InvoiceApplyHeader.FIELD_BILL_TO_PHONE,
                InvoiceApplyHeader.FIELD_INVOICE_COLOR,
                InvoiceApplyHeader.FIELD_INVOICE_TYPE,
                InvoiceApplyHeader.FIELD_REMARK);
        if(CollectionUtils.isNotEmpty(headers))
        {
            List<String> keys = new ArrayList<>();
            for (InvoiceApplyHeader invoiceApplyHeader : headers)
            {
                keys.add(Constants.CACHE_KEY_PREFIX+":"+invoiceApplyHeader.getApplyHeaderNumber());
            }
            redisHelper.delKeys(keys);
        }
    }

}

