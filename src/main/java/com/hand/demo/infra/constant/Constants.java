package com.hand.demo.infra.constant;

import org.stringtemplate.v4.ST;

/**
 * Utils
 */
public class Constants {
    public static final String LOV_CODE_TYPE = "HEXAM-47837-TYPE";
    public static final String LOV_CODE_COLOR = "HEXAM-47837-COLOUR";
    public static final String LOV_CODE_STATUS = "HEXAM-47837-STATUS";
    public static final String MESSAGE_ERROR_TYPE_DOESNT_MATCH = "hexam-47837.inv_type_do_not_exist.error";
    public static final String MESSAGE_ERROR_COLOR_DOESNT_MATCH = "hexam-47837.inv_color_do_not_exist.error";
    public static final String MESSAGE_ERROR_STATUS_DOESNT_MATCH = "hexam-47837.inv_status_do_not_exist.error";
    public static final String MESSAGE_ERROR_NOT_FOUND = "hexam-47837.inv_do_not_exist.error";
    public static final String CODE_RULE_HEADER = "HEXAM-INV-HEADER-47837";
    public static final String MESSAGE_ERROR_HEADER_ID_CANNOT_BE_NULL = "hexam-47837.inv_line_id_header_cannot_be_null";
    public static final String MESSAGE_ERROR_INV_LINE_NOT_FOUND = "hexam-47837.inv_line_cannot_be_found";
    public static final String TEMPLATE_IMPORT_CODE = "HEXAM-47837";
    public static final String SHEET_HEADER_NAME = "Invoice Header";
    public static final String SHEET_LINE_NAME = "Invoice Line";
    public static final String MESSAGE_ERROR_INVALID_LOV = "hexam-47837.lov_value_doesnt_match";
    public static final String CACHE_KEY_PREFIX_DELETE_HEADER = "hexam-47837:invoice-header:deleted";
    public static final String CACHE_KEY_PREFIX = "hexam-47837:invoice-header";
    public static final String PRODUCER_KEY_HEADER = "invoiceInfo_47837";
    public static final String MESSAGE_ERROR_DATA_NULL = "Data cannot be null or empty";
    public static final String MESSAGE_ERROR_MISSING_TENANT_OR_EMPLOYEE = "Missing required parameters: tenantId or employeeId";
    public static final String JOB_HANDLER = "hexam-47837";
    private Constants() {}

}
