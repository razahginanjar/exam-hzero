package com.hand.demo.api.controller.v1;

import com.hand.demo.api.dto.InvApplyHeaderDTO;
import com.hand.demo.api.dto.ReportExportDTO;
import com.hand.demo.config.SwaggerTags;
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
import com.hand.demo.app.service.InvoiceApplyHeaderService;
import com.hand.demo.domain.entity.InvoiceApplyHeader;
import com.hand.demo.domain.repository.InvoiceApplyHeaderRepository;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Invoice Apply Header Table(InvoiceApplyHeader)表控制层
 *
 * @author razah
 * @since 2024-12-03 09:28:06
 */
@Api(tags = SwaggerTags.HEADER)
@RestController("invoiceApplyHeaderController.v1")
@RequestMapping("/v1/{organizationId}/invoice-apply-headers")
public class InvoiceApplyHeaderController extends BaseController {

    @Autowired
    private InvoiceApplyHeaderRepository invoiceApplyHeaderRepository;

    @Autowired
    private InvoiceApplyHeaderService invoiceApplyHeaderService;

    @ApiOperation(value = "Invoice Apply Header Table列表")
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    public ResponseEntity<Page<InvApplyHeaderDTO>> list(
            InvApplyHeaderDTO invoiceApplyHeader,
            @PathVariable Long organizationId,
            @ApiIgnore @SortDefault(value = InvoiceApplyHeader.FIELD_APPLY_HEADER_ID,
            direction = Sort.Direction.DESC) PageRequest pageRequest) {
        invoiceApplyHeader.setTenantId(organizationId);
        Page<InvApplyHeaderDTO> list =
                invoiceApplyHeaderService.selectList(pageRequest, invoiceApplyHeader);
        return Results.success(list);
    }

    @ApiOperation(value = "Invoice Apply Header Table明细")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    @GetMapping("/{applyHeaderId}/detail")
    public ResponseEntity<InvApplyHeaderDTO> detail(
            @PathVariable Long applyHeaderId,
            @PathVariable Long organizationId) {
        InvApplyHeaderDTO invApplyHeaderDTO =
                invoiceApplyHeaderService.selectDetail(applyHeaderId, organizationId);
        return Results.success(invApplyHeaderDTO);
    }

    @ApiOperation(value = "创建或更新Invoice Apply Header Table")
    @Permission(level = ResourceLevel.ORGANIZATION)
    //@ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    @PostMapping
    public ResponseEntity<List<InvApplyHeaderDTO>> save(
            @PathVariable Long organizationId,
            @RequestBody List<InvApplyHeaderDTO> invApplyHeaderDTOS) {
        validObject(invApplyHeaderDTOS);
        SecurityTokenHelper.validTokenIgnoreInsert(invApplyHeaderDTOS);
        invApplyHeaderDTOS.forEach(item -> item.setTenantId(organizationId));
        invoiceApplyHeaderService.saveData(invApplyHeaderDTOS);
        return Results.success(invApplyHeaderDTOS);
    }

    @ApiOperation(value = "删除Invoice Apply Header Table")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(@RequestBody List<InvApplyHeaderDTO>
                                                invoiceApplyHeaders,
                                    @PathVariable Long organizationId) {
        SecurityTokenHelper.validToken(invoiceApplyHeaders);
        invoiceApplyHeaders.forEach(item ->
                item.setTenantId(organizationId));
        invoiceApplyHeaderService.delete(invoiceApplyHeaders);
        return Results.success();
    }


    @Permission(level = ResourceLevel.ORGANIZATION)
    @ApiOperation(value = "分页查询用户")
    @GetMapping(
            path = "/export"
    )
    @ExcelExport(value = InvApplyHeaderDTO.class)
    public ResponseEntity<List<InvApplyHeaderDTO>> export(
            InvApplyHeaderDTO invApplyHeaderDTO,
            ExportParam param,
            HttpServletResponse response,
            @PathVariable Long organizationId) {

        return Results.success(invoiceApplyHeaderService.exportData(invApplyHeaderDTO));
    }


    @ApiOperation(value = "Invoice Apply Header Table列表")
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping(
            path = "/filter"
    )
    public ResponseEntity<ReportExportDTO> filterList(
            @PathVariable Long organizationId,
            ReportExportDTO reportExportDTO) {
        ReportExportDTO reportExportDTO1 = invoiceApplyHeaderService.selectReport(organizationId, reportExportDTO);
        return Results.success(reportExportDTO1);
    }
}

