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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

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
    private Long applyLineId;

    @ApiModelProperty(value = "Foreign Key to Header Table", required = true)
    @NotNull
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
    private String contentName;

    @ApiModelProperty(value = "Calculated: total_amount - tax_amount")
    private BigDecimal excludeTaxAmount;

    @ApiModelProperty(value = "Invoice Name")
    private String invoiceName;

    @ApiModelProperty(value = "Quantity", required = true)
    @NotNull
    private BigDecimal quantity;

    @ApiModelProperty(value = "Additional Notes")
    private String remark;

    @ApiModelProperty(value = "Calculated: total_amount * tax_rate")
    private BigDecimal taxAmount;

    @ApiModelProperty(value = "Tax Classification Number")
    private String taxClassificationNumber;

    @ApiModelProperty(value = "Tax Rate (e.g., 0.08)", required = true)
    @NotNull
    private BigDecimal taxRate;

    @ApiModelProperty(value = "Tenant Identifier", required = true)
    @NotNull
    private Long tenantId;

    @ApiModelProperty(value = "Calculated: unit_price * quantity")
    private BigDecimal totalAmount;

    @ApiModelProperty(value = "Unit Price", required = true)
    @NotNull
    private BigDecimal unitPrice;


}

