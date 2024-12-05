package com.hand.demo.app.service;

import com.hand.demo.api.dto.InvApplyHeaderDTO;
import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import com.hand.demo.domain.entity.InvoiceApplyHeader;

import java.util.List;

/**
 * Invoice Apply Header Table(InvoiceApplyHeader)应用服务
 *
 * @author razah
 * @since 2024-12-03 09:28:06
 */
public interface InvoiceApplyHeaderService {

    /**
     * 查询数据
     *
     * @param pageRequest         分页参数
     * @param invoiceApplyHeaders 查询条件
     * @return 返回值
     */
    Page<InvApplyHeaderDTO> selectList(PageRequest pageRequest, InvoiceApplyHeader invoiceApplyHeaders);

    /**
     * 保存数据
     *
     * @param invoiceApplyHeaders 数据
     */
    void saveData(List<InvoiceApplyHeader> invoiceApplyHeaders);

    void delete(List<InvoiceApplyHeader> invoiceApplyHeaders);
    InvoiceApplyHeader selectOne(Long id);
    InvApplyHeaderDTO selectDetail(Long invHeaderId, Long tenantId);
    InvoiceApplyHeader selectDetailSelective(InvoiceApplyHeader invoiceApplyHeader);
}

