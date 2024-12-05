package com.hand.demo.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import com.hand.demo.infra.constant.Constants;
import io.choerodon.core.exception.CommonException;
import lombok.extern.slf4j.Slf4j;
import org.hzero.boot.imported.app.service.BatchValidatorHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidator;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidators;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

@ImportValidators(
        {
                @ImportValidator(templateCode = Constants.TEMPLATE_IMPORT_CODE,
                        sheetName = Constants.SHEET_LINE_NAME)
        }
)
@Slf4j
public class ValidateImportLineService extends BatchValidatorHandler {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Override
    public boolean validate(List<String> data) {
        for (String datum : data) {
            InvoiceApplyHeader invoiceApplyHeader;
            try{
                invoiceApplyHeader = objectMapper.readValue(datum, InvoiceApplyHeader.class);
                if(Objects.nonNull(invoiceApplyHeader.getApplyHeaderNumber())
                    && !invoiceApplyHeader.getApplyHeaderNumber().isEmpty())
                {
                    InvoiceApplyHeader invoiceApplyHeader1
                            = invoiceApplyHeaderRepository.selectOne(invoiceApplyHeader);
                    if(Objects.isNull(invoiceApplyHeader1))
                    {
                        throw new CommonException(Constants.MESSAGE_ERROR_NOT_FOUND);
                    }
                }
            }catch (Exception e){
                log.error(e.getMessage());
                return false;
            }
        }
        return true;
    }
}
