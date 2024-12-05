package com.hand.demo.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.InvoiceInfoQueue;

import java.util.List;

/**
 * Redis Message Queue Table(InvoiceInfoQueue)应用服务
 *
 * @author razah
 * @since 2024-12-03 09:28:20
 */
public interface InvoiceInfoQueueService {

    /**
     * 查询数据
     *
     * @param pageRequest       分页参数
     * @param invoiceInfoQueues 查询条件
     * @return 返回值
     */
    Page<InvoiceInfoQueue> selectList(PageRequest pageRequest, InvoiceInfoQueue invoiceInfoQueues);

    /**
     * 保存数据
     *
     * @param invoiceInfoQueues 数据
     */
    void saveData(List<InvoiceInfoQueue> invoiceInfoQueues);

}

