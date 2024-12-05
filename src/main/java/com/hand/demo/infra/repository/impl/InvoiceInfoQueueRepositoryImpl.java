package com.hand.demo.infra.repository.impl;

import org.apache.commons.collections.CollectionUtils;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.stereotype.Component;
import com.hand.demo.domain.entity.InvoiceInfoQueue;
import com.hand.demo.domain.repository.InvoiceInfoQueueRepository;
import com.hand.demo.infra.mapper.InvoiceInfoQueueMapper;

import javax.annotation.Resource;
import java.util.List;

/**
 * Redis Message Queue Table(InvoiceInfoQueue)资源库
 *
 * @author razah
 * @since 2024-12-03 09:28:20
 */
@Component
public class InvoiceInfoQueueRepositoryImpl extends BaseRepositoryImpl<InvoiceInfoQueue> implements InvoiceInfoQueueRepository {
    @Resource
    private InvoiceInfoQueueMapper invoiceInfoQueueMapper;

    @Override
    public List<InvoiceInfoQueue> selectList(InvoiceInfoQueue invoiceInfoQueue) {
        return invoiceInfoQueueMapper.selectList(invoiceInfoQueue);
    }

    @Override
    public InvoiceInfoQueue selectByPrimary(Long id) {
        InvoiceInfoQueue invoiceInfoQueue = new InvoiceInfoQueue();
        invoiceInfoQueue.setId(id);
        List<InvoiceInfoQueue> invoiceInfoQueues = invoiceInfoQueueMapper.selectList(invoiceInfoQueue);
        if (invoiceInfoQueues.size() == 0) {
            return null;
        }
        return invoiceInfoQueues.get(0);
    }

}

