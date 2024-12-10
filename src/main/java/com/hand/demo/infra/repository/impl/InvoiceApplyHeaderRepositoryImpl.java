package com.hand.demo.infra.repository.impl;

import com.hand.demo.api.dto.InvApplyHeaderDTO;
import org.apache.commons.collections.CollectionUtils;
import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.stereotype.Component;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.mapper.InvoiceApplyHeaderMapper;

import javax.annotation.Resource;
import java.util.List;

/**
 * Invoice Apply Header Table(InvoiceApplyHeader)资源库
 *
 * @author razah
 * @since 2024-12-03 09:28:06
 */
@Component
public class InvoiceApplyHeaderRepositoryImpl extends BaseRepositoryImpl<InvoiceApplyHeader> implements InvoiceApplyHeaderRepository {
    @Resource
    private InvoiceApplyHeaderMapper invoiceApplyHeaderMapper;

    @Override
    public List<InvApplyHeaderDTO> selectList(InvApplyHeaderDTO invoiceApplyHeader) {
        return invoiceApplyHeaderMapper.selectList(invoiceApplyHeader);
    }

    @Override
    public InvApplyHeaderDTO selectByPrimary(Long applyHeaderId) {
        InvApplyHeaderDTO invoiceApplyHeader = new InvApplyHeaderDTO();
        invoiceApplyHeader.setApplyHeaderId(applyHeaderId);
        List<InvApplyHeaderDTO> invoiceApplyHeaders =
                invoiceApplyHeaderMapper.selectList(invoiceApplyHeader);
        if (invoiceApplyHeaders.size() == 0) {
            return null;
        }
        return invoiceApplyHeaders.get(0);
    }

//    @Override
//    public List<InvApplyHeaderDTO> batchUpdateByPrimaryKeySelective(List<InvApplyHeaderDTO> list) {
//        this.batchDml
//        return super.batchUpdateByPrimaryKeySelective(list);
//    }
}

