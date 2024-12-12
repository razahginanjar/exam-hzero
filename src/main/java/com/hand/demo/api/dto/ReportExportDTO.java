package com.hand.demo.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ReportExportDTO {
    private String applyNumberFrom;
    private String applyNumberTo;
    private Date submitTimeFrom;
    private Date submitTimeTo;
    private Date createdDateFrom;
    private Date createdDateTo;
    private String InvoiceType;
}
