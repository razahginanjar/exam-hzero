package com.hand.demo.api.controller.v1;

import com.hand.demo.api.dto.InvApplyHeaderDTO;
import com.hand.demo.config.SwaggerTags;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.hzero.export.annotation.ExcelExport;
import org.hzero.export.vo.ExportParam;
import org.hzero.mybatis.helper.SecurityTokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hand.demo.app.service.InvoiceApplyLineService;
import com.hand.demo.domain.entity.InvoiceApplyLine;
import com.hand.demo.domain.repository.InvoiceApplyLineRepository;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Invoice Apply Line Table(InvoiceApplyLine)表控制层
 *
 * @author razah
 * @since 2024-12-03 09:28:00
 */
@Api(tags = SwaggerTags.LINE)
@RestController("invoiceApplyLineController.v1")
@RequestMapping("/v1/{organizationId}/invoice-apply-lines")
public class InvoiceApplyLineController extends BaseController {

    @Autowired
    private InvoiceApplyLineRepository invoiceApplyLineRepository;

    @Autowired
    private InvoiceApplyLineService invoiceApplyLineService;

    @ApiOperation(value = "Invoice Apply Line Table列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    public ResponseEntity<Page<InvoiceApplyLine>> list(
            InvoiceApplyLine invoiceApplyLine,
            @PathVariable Long organizationId,
            @ApiIgnore @SortDefault(value = InvoiceApplyLine.FIELD_APPLY_LINE_ID,
            direction = Sort.Direction.DESC) PageRequest pageRequest) {
        invoiceApplyLine.setTenantId(organizationId);
        Page<InvoiceApplyLine> list = invoiceApplyLineService.selectList(pageRequest, invoiceApplyLine);
        return Results.success(list);
    }

    @ApiOperation(value = "Invoice Apply Line Table明细")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{applyLineId}/detail")
    public ResponseEntity<InvoiceApplyLine> detail(
            @PathVariable Long applyLineId,
            @PathVariable Long organizationId) {
        InvoiceApplyLine invoiceApplyLine = invoiceApplyLineRepository
                .selectByPrimary(applyLineId);
        return Results.success(invoiceApplyLine);
    }

    @ApiOperation(value = "创建或更新Invoice Apply Line Table")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<List<InvoiceApplyLine>> save(
            @PathVariable Long organizationId,
            @RequestBody List<InvoiceApplyLine> invoiceApplyLines) {
        validObject(invoiceApplyLines);
        SecurityTokenHelper.validTokenIgnoreInsert(invoiceApplyLines);
        invoiceApplyLines.forEach(item -> item.setTenantId(organizationId));
        invoiceApplyLineService.saveData(invoiceApplyLines);
        return Results.success(invoiceApplyLines);
    }

    @ApiOperation(value = "删除Invoice Apply Line Table")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(
            @RequestBody List<InvoiceApplyLine> invoiceApplyLines,
            @PathVariable Long organizationId) {
        SecurityTokenHelper.validToken(invoiceApplyLines);
        invoiceApplyLines.forEach(item -> item.setTenantId(organizationId));
        invoiceApplyLineService.removeData(invoiceApplyLines);
        return Results.success();
    }


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "分页查询用户")
    @GetMapping(
            path = "/export"
    )
    @ExcelExport(value = InvoiceApplyLine.class)
    @ProcessLovValue(
            targetField = BaseConstants.FIELD_BODY
    )
    public ResponseEntity<List<InvoiceApplyLine>> export(
            InvoiceApplyLine invoiceApplyLine,
            ExportParam param,
            HttpServletResponse response,
            @PathVariable Long organizationId) {

        return Results.success(invoiceApplyLineService.exportData(invoiceApplyLine));
    }
}

