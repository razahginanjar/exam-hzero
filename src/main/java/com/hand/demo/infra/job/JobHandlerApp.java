package com.hand.demo.infra.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.Constants;
import io.choerodon.core.exception.CommonException;
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
import java.util.stream.Collectors;

@JobHandler(value = "hexam-47837")
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

        InvoiceApplyHeader invoiceApplyHeader = new InvoiceApplyHeader();
        invoiceApplyHeader.setTenantId(Long.valueOf(map.get("tenantId")));
        invoiceApplyHeader.setDelFlag(1);
        invoiceApplyHeader.setInvoiceColor("R");
        invoiceApplyHeader.setApplyStatus("F");
        invoiceApplyHeader.setInvoiceType("E");
        List<InvoiceApplyHeader> invoiceApplyHeaders
                = invoiceApplyHeaderRepository.selectList(invoiceApplyHeader);
        List<String> messages = new ArrayList<>();

        if(invoiceApplyHeaders == null || invoiceApplyHeaders.isEmpty()){
            return ReturnT.SUCCESS;
        }
        invoiceApplyHeaders.forEach(
               invoiceApplyHeader1 ->
               {
                   try {
                       String s = objectMapper.writeValueAsString(invoiceApplyHeader1);
                       messages.add(s);
                   } catch (JsonProcessingException e) {
                       throw new CommonException(e);
                   }
               }
        );

        redisQueueHelper.pushAll(Constants.PRODUCER_KEY_HEADER,messages);
        return ReturnT.SUCCESS;
    }
}
