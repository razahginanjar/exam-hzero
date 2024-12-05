package com.hand.demo.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hzero.export.annotation.ExcelColumn;
import org.hzero.export.annotation.ExcelSheet;

/**
 * Invoice Apply Line Table(InvoiceApplyLine)实体类
 *
 * @author razah
 * @since 2024-12-03 09:27:59
 */

@Getter
@Setter
@ApiModel("Invoice Apply Line Table")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@ExcelSheet(en = "Invoice Line Information")
@Accessors(chain = true)
@Table(name = "todo_invoice_apply_line")
public class InvoiceApplyLine extends AuditDomain {
    private static final long serialVersionUID = 901718466763308509L;

    public static final String FIELD_APPLY_LINE_ID = "applyLineId";
    public static final String FIELD_APPLY_HEADER_ID = "applyHeaderId";
    public static final String FIELD_ATTRIBUTE1 = "attribute1";
    public static final String FIELD_ATTRIBUTE2 = "attribute2";
    public static final String FIELD_ATTRIBUTE3 = "attribute3";
    public static final String FIELD_ATTRIBUTE4 = "attribute4";
    public static final String FIELD_ATTRIBUTE5 = "attribute5";
    public static final String FIELD_CONTENT_NAME = "contentName";
    public static final String FIELD_EXCLUDE_TAX_AMOUNT = "excludeTaxAmount";
    public static final String FIELD_INVOICE_NAME = "invoiceName";
    public static final String FIELD_QUANTITY = "quantity";
    public static final String FIELD_REMARK = "remark";
    public static final String FIELD_TAX_AMOUNT = "taxAmount";
    public static final String FIELD_TAX_CLASSIFICATION_NUMBER = "taxClassificationNumber";
    public static final String FIELD_TAX_RATE = "taxRate";
    public static final String FIELD_TENANT_ID = "tenantId";
    public static final String FIELD_TOTAL_AMOUNT = "totalAmount";
    public static final String FIELD_UNIT_PRICE = "unitPrice";

    @ApiModelProperty("Primary Key")
    @Id
    @GeneratedValue
    @ExcelColumn(en = "Apply Line Id")
    private Long applyLineId;

    @ApiModelProperty(value = "Foreign Key to Header Table", required = true)
    @NotNull
    @ExcelColumn(en = "Apply Header Id", order = 2)
    private Long applyHeaderId;

    @ApiModelProperty(value = "")
    private String attribute1;

    @ApiModelProperty(value = "")
    private String attribute2;

    @ApiModelProperty(value = "")
    private String attribute3;

    @ApiModelProperty(value = "")
    private String attribute4;

    @ApiModelProperty(value = "")
    private String attribute5;

    @ApiModelProperty(value = "Content Name")
    @ExcelColumn(en = "Content Name", order = 3)
    private String contentName;

    @ApiModelProperty(value = "Calculated: total_amount - tax_amount")
    @ExcelColumn(en = "Exclude Tax Amount", order = 4)
    private BigDecimal excludeTaxAmount;

    @ApiModelProperty(value = "Invoice Name")
    @ExcelColumn(en = "Invoice Name", order = 5)
    private String invoiceName;

    @ApiModelProperty(value = "Quantity", required = true)
    @ExcelColumn(en = "Quantity", order = 6)
    @NotNull
    private BigDecimal quantity;

    @ApiModelProperty(value = "Additional Notes")
    @ExcelColumn(en = "Remark", order = 7)
    private String remark;

    @ApiModelProperty(value = "Calculated: total_amount * tax_rate")
    @ExcelColumn(en = "Tax Amount", order = 8)
    private BigDecimal taxAmount;

    @ApiModelProperty(value = "Tax Classification Number")
    @ExcelColumn(en = "Tax Classification Number", order = 9)
    private String taxClassificationNumber;

    @ApiModelProperty(value = "Tax Rate (e.g., 0.08)", required = true)
    @ExcelColumn(en = "Tax Rate", order = 10)
    @NotNull
    private BigDecimal taxRate;

    @ApiModelProperty(value = "Tenant Identifier", required = true)
    @ExcelColumn(en = "Tenant Id", order = 11)
    @NotNull
    private Long tenantId;

    @ApiModelProperty(value = "Calculated: unit_price * quantity")
    @ExcelColumn(en = "Total Amount", order = 12)
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "Unit Price", required = true)
    @ExcelColumn(en = "Unit Price", order = 13)
    @NotNull
    private BigDecimal unitPrice;

    @Transient
    private List<Long> headerIds;

}

