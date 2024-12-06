package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.app.service.InvoiceApplyLineService;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import com.hand.demo.infra.constant.Constants;
import io.choerodon.core.oauth.DetailsHelper;
import lombok.extern.slf4j.Slf4j;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ImportService(templateCode = Constants.TEMPLATE_IMPORT_CODE,
        sheetName = Constants.SHEET_LINE_NAME)
@Slf4j
public class BatchImportLineServiceImpl extends BatchImportHandler {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    InvoiceApplyLineService invoiceApplyLineService;
    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Override
    public Boolean doImport(List<String> data) {
        if (data == null || data.isEmpty()) {
            log.warn(Constants.MESSAGE_ERROR_DATA_NULL);
            return false;
        }

        List<InvoiceApplyLine> lineList = new ArrayList<>();
        for (String datum : data) {
            try {
                InvoiceApplyLine invoiceApplyLine =
                        objectMapper.readValue(datum, InvoiceApplyLine.class);
                if(Objects.nonNull(invoiceApplyLine.getApplyLineId()))
                {
                    InvoiceApplyLine line
                            = invoiceApplyLineRepository.selectByPrimary(invoiceApplyLine.getApplyLineId());
                    invoiceApplyLine.setObjectVersionNumber(line.getObjectVersionNumber());

                }
                invoiceApplyLine.setTenantId(DetailsHelper.getUserDetails().getTenantId());
                lineList.add(invoiceApplyLine);
            } catch (IOException e) {
                log.error("Failed to parse data: {}, error: {}", datum, e.getMessage(), e);
                return false;
            }
        }
        invoiceApplyLineService.saveData(lineList);
        log.info("Import completed successfully. Inserted: {}", lineList.size());
        return true;
    }
}
