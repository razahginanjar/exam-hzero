package com.hand.demo.infra.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.api.dto.InvApplyHeaderDTO;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.Constants;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import lombok.extern.slf4j.Slf4j;
import org.hzero.boot.scheduler.infra.annotation.JobHandler;
import org.hzero.boot.scheduler.infra.enums.ReturnT;
import org.hzero.boot.scheduler.infra.handler.IJobHandler;
import org.hzero.boot.scheduler.infra.tool.SchedulerTool;
import org.hzero.core.redis.RedisQueueHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JobHandler(value = Constants.JOB_HANDLER)
@Slf4j
public class JobHandlerApp implements IJobHandler{
    @Autowired
    InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RedisQueueHelper redisQueueHelper;

    @Override
    public ReturnT execute(Map<String, String> map,
                           SchedulerTool tool) {
        try {
            // Validate and extract parameters
            Long tenantId = Long.valueOf(map.get("tenantId"));
            String employeeId = map.get("employeeId");

            if (tenantId == null || employeeId == null) {
                log.error(Constants.MESSAGE_ERROR_MISSING_TENANT_OR_EMPLOYEE);
                return ReturnT.FAILURE;
            }

            // Query database for invoice headers
            InvApplyHeaderDTO query = new InvApplyHeaderDTO();
            query.setTenantId(tenantId);
            query.setDelFlag(1);
            query.setInvoiceColor("R");
            query.setApplyStatus("F");
            query.setInvoiceType("E");

            List<InvApplyHeaderDTO> invoiceApplyHeaders =
                    invoiceApplyHeaderRepository.selectList(query);

            // Exit if no data
            if (invoiceApplyHeaders == null || invoiceApplyHeaders.isEmpty()) {
                log.info("No matching invoice headers found.");
                return ReturnT.SUCCESS;
            }

            // Build and push messages to Redis queue
            List<String> messages = new ArrayList<>();
            for (InvoiceApplyHeader invoice : invoiceApplyHeaders) {
                Map<String, Object> mapper = objectMapper.convertValue(invoice, Map.class);
                mapper.put("userDetails", objectMapper.writeValueAsString(DetailsHelper.getUserDetails()));
                mapper.put("employeeId", employeeId);
                messages.add(objectMapper.writeValueAsString(mapper));
            }

            redisQueueHelper.pushAll(Constants.PRODUCER_KEY_HEADER, messages);
            log.info("Processed {} invoice headers successfully.", invoiceApplyHeaders.size());
            return ReturnT.SUCCESS;

        } catch (NumberFormatException e) {
            log.error("Invalid number format: {}", e.getMessage(), e);
            return ReturnT.FAILURE;
        } catch (JsonProcessingException e) {
            log.error("JSON processing error: {}", e.getMessage(), e);
            throw new CommonException("Failed to process JSON", e);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage(), e);
            throw new CommonException("Unexpected error occurred", e);
        }
    }
}
