package com.hand.demo.api.dto;

import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.infra.constant.Constants;
import lombok.Getter;
import lombok.Setter;
import org.hzero.export.annotation.ExcelColumn;
import org.hzero.export.annotation.ExcelSheet;

import javax.persistence.Transient;
import java.util.List;

@Getter
@Setter
@ExcelSheet(en = "Invoice Header Information")
public class InvApplyHeaderDTO extends InvoiceApplyHeader {

    @ExcelColumn(en = "Apply Status Meaning", order = 17, lovCode = Constants.LOV_CODE_STATUS)
    private String applyStatusMeaning;

    @ExcelColumn(en = "Invoice Color Meaning", order = 18, lovCode = Constants.LOV_CODE_COLOR)
    private String invoiceColorMeaning;

    @ExcelColumn(en = "Invoice Type Meaning", order = 19, lovCode = Constants.LOV_CODE_TYPE)
    private String invoiceTypeMeaning;

    @Transient
    @ExcelColumn(promptCode = "children",
            promptKey = "children", child = true)
    private List<InvoiceApplyLine> invoiceApplyLines;

    private String realName;

    private String invoiceNames;
}
