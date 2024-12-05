package com.hand.demo.domain.repository;

import org.hzero.mybatis.base.BaseRepository;
import com.hand.demo.domain.entity.InvoiceApplyLine;

import java.util.List;

/**
 * Invoice Apply Line Table(InvoiceApplyLine)资源库
 *
 * @author razah
 * @since 2024-12-03 09:27:59
 */
public interface InvoiceApplyLineRepository extends BaseRepository<InvoiceApplyLine> {
    /**
     * 查询
     *
     * @param invoiceApplyLine 查询条件
     * @return 返回值
     */
    List<InvoiceApplyLine> selectList(InvoiceApplyLine invoiceApplyLine);

    /**
     * 根据主键查询（可关联表）
     *
     * @param applyLineId 主键
     * @return 返回值
     */
    InvoiceApplyLine selectByPrimary(Long applyLineId);
}
