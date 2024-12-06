package com.hand.demo.infra.listener;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.app.service.InvoiceInfoQueueService;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceInfoQueue;
import com.hand.demo.domain.repository.InvoiceInfoQueueRepository;
import com.hand.demo.infra.constant.Constants;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import lombok.extern.slf4j.Slf4j;
import org.hzero.core.redis.RedisQueueHelper;
import org.hzero.core.redis.handler.IBatchQueueHandler;
import org.hzero.core.redis.handler.QueueHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@QueueHandler(value = Constants.PRODUCER_KEY_HEADER)
@Slf4j
public class MessageListener implements IBatchQueueHandler {
    @Autowired
    InvoiceInfoQueueService invoiceInfoQueueService;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void process(List<String> messages) {
        InvoiceInfoQueue invoiceInfoQueue = new InvoiceInfoQueue();
        List<InvoiceInfoQueue> list = new ArrayList<>();
        try {
            Map<String, Object> map1 = objectMapper.readValue(messages.get(0), Map.class);
            CustomUserDetails customUserDetails =
                    objectMapper.readValue(map1.get("userDetails").toString(), CustomUserDetails.class);
            DetailsHelper.setCustomUserDetails(customUserDetails);
        }catch (Exception e){
            log.error(e.getMessage());
            throw new CommonException(e.getMessage());
        }
        for (String message : messages) {
            try {
                Map<String, Object> map = objectMapper.readValue(message, Map.class);
                if(map != null)
                {
                    invoiceInfoQueue.setEmployeeId(map.get("employeeId").toString());
                    invoiceInfoQueue.setTenantId(Long.valueOf(map.get(InvoiceApplyHeader.FIELD_TENANT_ID).toString()));
                }
                invoiceInfoQueue.setContent(message);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            list.add(invoiceInfoQueue);
        }
        invoiceInfoQueueService.saveData(list);
    }
}
