package com.hand.demo.domain.repository;

import com.hand.demo.api.dto.InvApplyHeaderDTO;
import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.InvoiceApplyHeader;

import java.util.List;

/**
 * Invoice Apply Header Table(InvoiceApplyHeader)资源库
 *
 * @author razah
 * @since 2024-12-03 09:28:06
 */
public interface InvoiceApplyHeaderRepository extends BaseRepository<InvoiceApplyHeader> {
    /**
     * 查询
     *
     * @param invoiceApplyHeader 查询条件
     * @return 返回值
     */
    List<InvApplyHeaderDTO> selectList(InvApplyHeaderDTO invoiceApplyHeader);

    /**
     * 根据主键查询（可关联表）
     *
     * @param applyHeaderId 主键
     * @return 返回值
     */
    InvApplyHeaderDTO selectByPrimary(Long applyHeaderId);
}
