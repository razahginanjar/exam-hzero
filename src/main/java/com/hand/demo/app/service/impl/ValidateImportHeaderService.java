package com.hand.demo.app.service.impl;

import com.hand.demo.infra.constant.Constants;
import org.hzero.boot.imported.app.service.BatchValidatorHandler;
import org.hzero.boot.imported.app.service.ValidatorHandler;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidator;
import org.hzero.boot.imported.infra.validator.annotation.ImportValidators;

import java.util.List;

@ImportValidators(
        {
                @ImportValidator(templateCode = Constants.TEMPLATE_IMPORT_CODE,
                        sheetName = Constants.SHEET_HEADER_NAME)
        }
)
public class ValidateImportHeaderService extends BatchValidatorHandler {

    @Override
    public boolean validate(List<String> data) {
        return false;
    }
}
