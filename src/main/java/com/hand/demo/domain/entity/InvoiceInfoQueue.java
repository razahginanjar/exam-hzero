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
 * Redis Message Queue Table(InvoiceInfoQueue)实体类
 *
 * @author razah
 * @since 2024-12-03 09:28:19
 */

@Getter
@Setter
@ApiModel("Redis Message Queue Table")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "todo_invoice_info_queue")
public class InvoiceInfoQueue extends AuditDomain {
    private static final long serialVersionUID = 442366189369146551L;

    public static final String FIELD_ID = "id";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_EMPLOYEE_ID = "employeeId";
    public static final String FIELD_TENANT_ID = "tenantId";

    @ApiModelProperty("Primary Key")
    @Id
    @GeneratedValue
    private Long id;

    @ApiModelProperty(value = "Message Content")
    private Object content;

    @ApiModelProperty(value = "Employee Identifier", required = true)
    @NotBlank
    private String employeeId;

    @ApiModelProperty(value = "Tenant Identifier")
    private Long tenantId;


}

