package com.hand.demo.domain.entity;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hand.demo.infra.constant.Constants;
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

import lombok.Getter;
import lombok.Setter;
import org.hzero.boot.platform.lov.annotation.LovValue;
import org.hzero.core.cache.CacheValue;
import org.hzero.core.cache.Cacheable;
import org.hzero.export.annotation.ExcelColumn;

/**
 * Invoice Apply Header Table(InvoiceApplyHeader)实体类
 *
 * @author razah
 * @since 2024-12-03 09:28:06
 */

@Getter
@Setter
@ApiModel("Invoice Apply Header Table")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "todo_invoice_apply_header")
public class InvoiceApplyHeader extends AuditDomain implements Cacheable, Serializable {
    private static final long serialVersionUID = -45223201537570491L;

    public static final String FIELD_APPLY_HEADER_ID = "applyHeaderId";
    public static final String FIELD_APPLY_HEADER_NUMBER = "applyHeaderNumber";
    public static final String FIELD_APPLY_STATUS = "applyStatus";
    public static final String FIELD_ATTRIBUTE1 = "attribute1";
    public static final String FIELD_ATTRIBUTE10 = "attribute10";
    public static final String FIELD_ATTRIBUTE11 = "attribute11";
    public static final String FIELD_ATTRIBUTE12 = "attribute12";
    public static final String FIELD_ATTRIBUTE13 = "attribute13";
    public static final String FIELD_ATTRIBUTE14 = "attribute14";
    public static final String FIELD_ATTRIBUTE15 = "attribute15";
    public static final String FIELD_ATTRIBUTE2 = "attribute2";
    public static final String FIELD_ATTRIBUTE3 = "attribute3";
    public static final String FIELD_ATTRIBUTE4 = "attribute4";
    public static final String FIELD_ATTRIBUTE5 = "attribute5";
    public static final String FIELD_ATTRIBUTE6 = "attribute6";
    public static final String FIELD_ATTRIBUTE7 = "attribute7";
    public static final String FIELD_ATTRIBUTE8 = "attribute8";
    public static final String FIELD_ATTRIBUTE9 = "attribute9";
    public static final String FIELD_BILL_TO_ADDRESS = "billToAddress";
    public static final String FIELD_BILL_TO_EMAIL = "billToEmail";
    public static final String FIELD_BILL_TO_PERSON = "billToPerson";
    public static final String FIELD_BILL_TO_PHONE = "billToPhone";
    public static final String FIELD_DEL_FLAG = "delFlag";
    public static final String FIELD_EXCLUDE_TAX_AMOUNT = "excludeTaxAmount";
    public static final String FIELD_INVOICE_COLOR = "invoiceColor";
    public static final String FIELD_INVOICE_TYPE = "invoiceType";
    public static final String FIELD_REMARK = "remark";
    public static final String FIELD_SUBMIT_TIME = "submitTime";
    public static final String FIELD_TAX_AMOUNT = "taxAmount";
    public static final String FIELD_TENANT_ID = "tenantId";
    public static final String FIELD_TOTAL_AMOUNT = "totalAmount";

    @ApiModelProperty("Primary Key")
    @Id
    @GeneratedValue
    @ExcelColumn(en = "Apply Header Id")
    private Long applyHeaderId;

    @ApiModelProperty(value = "Header Number", required = true)
    @NotBlank
    @ExcelColumn(en = "Apply Header Number", order = 2)
    private String applyHeaderNumber;

    @ApiModelProperty(value = "(Value Set)         D : Draft         S : Success         F : Fail         C : Canceled", required = true)
    @NotBlank
    @LovValue(lovCode = Constants.LOV_CODE_STATUS)
    @ExcelColumn(en = "Apply Status", lovCode = Constants.LOV_CODE_STATUS, order = 3)
    private String applyStatus;

    @ApiModelProperty(value = "")
    private String attribute1;

    @ApiModelProperty(value = "")
    private String attribute10;

    @ApiModelProperty(value = "")
    private String attribute11;

    @ApiModelProperty(value = "")
    private String attribute12;

    @ApiModelProperty(value = "")
    private String attribute13;

    @ApiModelProperty(value = "")
    private String attribute14;

    @ApiModelProperty(value = "")
    private String attribute15;

    @ApiModelProperty(value = "")
    private String attribute2;

    @ApiModelProperty(value = "")
    private String attribute3;

    @ApiModelProperty(value = "")
    private String attribute4;

    @ApiModelProperty(value = "")
    private String attribute5;

    @ApiModelProperty(value = "")
    private String attribute6;

    @ApiModelProperty(value = "")
    private String attribute7;

    @ApiModelProperty(value = "")
    private String attribute8;

    @ApiModelProperty(value = "")
    private String attribute9;

    @ApiModelProperty(value = "Bill Recipient Address")
    @ExcelColumn(en = "Bill To Address", order = 4)
    private String billToAddress;

    @ApiModelProperty(value = "Bill Recipient Email")
    @ExcelColumn(en = "Bill To Email", order = 5)
    private String billToEmail;

    @ApiModelProperty(value = "Bill Recipient Name")
    @ExcelColumn(en = "Bill To Person", order = 6)
    private String billToPerson;

    @ApiModelProperty(value = "Bill Recipient Phone")
    @ExcelColumn(en = "Bill To Phone", order = 7)
    private String billToPhone;

    @ApiModelProperty(value = "1: Deleted, 0: Normal")
    @ExcelColumn(en = "Delete Flag", order = 8)
    private Integer delFlag;

    @ApiModelProperty(value = "Sum of line exclude tax amounts", required = true)
    @NotNull
    @ExcelColumn(en = "Exclude Tax Amount", order = 9)
    private BigDecimal excludeTaxAmount;

    @ApiModelProperty(value = "(Value Set)         R : Red invoice         B : Blue invoice")
    @LovValue(lovCode = Constants.LOV_CODE_COLOR)
    @ExcelColumn(en = "Invoice Color", lovCode = Constants.LOV_CODE_COLOR, order = 10)
    private String invoiceColor;

    @ApiModelProperty(value = "(Value Set)         P : Paper invoice         E : E-invoice")
    @LovValue(lovCode = Constants.LOV_CODE_TYPE)
    @ExcelColumn(en = "Invoice Type", lovCode = Constants.LOV_CODE_TYPE, order = 11)
    private String invoiceType;

    @ApiModelProperty(value = "Additional Notes")
    @ExcelColumn(en = "Remark", order = 12)
    private String remark;

    @ApiModelProperty(value = "Submission Time")
    @ExcelColumn(en = "Submit Time", order = 13)
    private Date submitTime;

    @ApiModelProperty(value = "Sum of line tax amounts", required = true)
    @NotNull
    @ExcelColumn(en = "Tax Amount", order = 14)
    private BigDecimal taxAmount;

    @ApiModelProperty(value = "Tenant Identifier", required = true)
    @NotNull
    @ExcelColumn(en = "Tenant Id", order = 15)
    private Long tenantId;

    @ApiModelProperty(value = "Sum of line total amounts", required = true)
    @NotNull
    @ExcelColumn(en = "Total Amount", order = 16)
    private BigDecimal totalAmount;

    @Transient
    @CacheValue(key = "hexam-47837:invoice-header",
            primaryKey = "applyHeaderId",
            db = 1,
//            searchKey = "applyHeaderId",
            structure = CacheValue.DataStructure.MAP_OBJECT)
    private String idDetail;

}

