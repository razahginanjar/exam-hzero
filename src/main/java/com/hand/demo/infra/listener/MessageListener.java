package com.hand.demo.infra.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceInfoQueue;
import com.hand.demo.domain.repository.InvoiceInfoQueueRepository;
import com.hand.demo.infra.constant.Constants;
import io.choerodon.core.oauth.DetailsHelper;
import org.hzero.core.redis.RedisQueueHelper;
import org.hzero.core.redis.handler.IBatchQueueHandler;
import org.hzero.core.redis.handler.QueueHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@QueueHandler(value = Constants.PRODUCER_KEY_HEADER)
public class MessageListener implements IBatchQueueHandler {
    @Autowired
    InvoiceInfoQueueRepository invoiceInfoQueueRepository;


    @Override
    public void process(List<String> messages) {
        InvoiceInfoQueue invoiceInfoQueue = new InvoiceInfoQueue();
        List<InvoiceInfoQueue> list = new ArrayList<>();
        for (String message : messages) {
            invoiceInfoQueue.setContent(message);
            invoiceInfoQueue.setEmployeeId("47837");
            invoiceInfoQueue.setTenantId(0L);
            list.add(invoiceInfoQueue);
        }
        invoiceInfoQueueRepository.batchInsertSelective(list);
    }
}
