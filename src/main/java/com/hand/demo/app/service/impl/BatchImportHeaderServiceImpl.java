package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.api.dto.InvApplyHeaderDTO;
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.Constants;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import lombok.extern.slf4j.Slf4j;
import org.hzero.boot.imported.app.service.BatchImportHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ImportService(templateCode = Constants.TEMPLATE_IMPORT_CODE,
        sheetName = Constants.SHEET_HEADER_NAME)
@Slf4j
public class BatchImportHeaderServiceImpl extends BatchImportHandler {
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    InvoiceApplyHeaderService invoiceApplyHeaderService;

    @Autowired
    InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Override
    public Boolean doImport(List<String> data) {
        if (data == null || data.isEmpty()) {
            log.warn(Constants.MESSAGE_ERROR_DATA_NULL);
            return false;
        }

        Map<String, Object> args = getArgs(); // Assuming this is needed for external logic
        List<InvApplyHeaderDTO> listHeaders = new ArrayList<>();

        for (String datum : data) {
            try {
                // Parse each JSON string into an InvoiceApplyHeader object
                InvApplyHeaderDTO invoiceApplyHeader
                        = objectMapper.readValue(datum, InvApplyHeaderDTO.class);

                if (invoiceApplyHeader.getApplyHeaderNumber() != null) {
                    // Check if the header already exists
                    InvoiceApplyHeader queryHeader = new InvoiceApplyHeader();
                    queryHeader
                            .setApplyHeaderNumber(invoiceApplyHeader.getApplyHeaderNumber());
                    InvoiceApplyHeader existingHeader = invoiceApplyHeaderRepository.selectOne(queryHeader);

                    if (existingHeader == null) {
                        throw new CommonException(Constants.MESSAGE_ERROR_NOT_FOUND);
                    }

                    // Populate fields for updating the header
                    invoiceApplyHeader.setObjectVersionNumber(existingHeader.getObjectVersionNumber());
                    invoiceApplyHeader.setApplyHeaderId(existingHeader.getApplyHeaderId());
                }

                listHeaders.add(invoiceApplyHeader);

            } catch (IOException e) {
                // Log and terminate on parsing failure
                log.error("Failed to parse data: {}, error: {}", datum, e.getMessage(), e);
                return false;
            }
        }

        try {
            // Save the processed headers
            invoiceApplyHeaderService.saveData(listHeaders);
            log.info("Import completed successfully. Rows changed: {}", listHeaders.size());
            return true;
        } catch (Exception e) {
            // Log and propagate if save fails
            log.error("Failed to save data, error: {}", e.getMessage(), e);
            return false;
        }
    }
}
