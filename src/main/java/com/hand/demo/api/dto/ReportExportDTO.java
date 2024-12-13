package com.hand.demo.api.dto;

import com.hand.demo.infra.constant.Constants;
import lombok.Getter;
import lombok.Setter;
import org.hzero.boot.platform.lov.annotation.LovValue;
import org.yaml.snakeyaml.scanner.Constant;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ReportExportDTO {
    private String applyNumberFrom;
    private String applyNumberTo;
    private LocalDate submitTimeFrom;
    private LocalDate submitTimeTo;
    private LocalDate createdDateFrom;
    private LocalDate createdDateTo;
    @LovValue(lovCode = Constants.LOV_CODE_TYPE)
    private String InvoiceType;

    private List<String> applyStatuses;

    private List<InvApplyHeaderDTO> headerDTOS;

    private String tenantName;
}
