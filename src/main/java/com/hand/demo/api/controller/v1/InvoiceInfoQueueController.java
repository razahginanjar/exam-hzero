package com.hand.demo.api.controller.v1;

import com.hand.demo.config.SwaggerTags;
import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.hzero.mybatis.helper.SecurityTokenHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hand.demo.app.service.InvoiceInfoQueueService;
import com.hand.demo.domain.entity.InvoiceInfoQueue;
import com.hand.demo.domain.repository.InvoiceInfoQueueRepository;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * Redis Message Queue Table(InvoiceInfoQueue)表控制层
 *
 * @author razah
 * @since 2024-12-03 09:28:20
 */
@Api(tags = SwaggerTags.QUEUE)
@RestController("invoiceInfoQueueController.v1")
@RequestMapping("/v1/{organizationId}/invoice-info-queues")
public class InvoiceInfoQueueController extends BaseController {

    @Autowired
    private InvoiceInfoQueueRepository invoiceInfoQueueRepository;

    @Autowired
    private InvoiceInfoQueueService invoiceInfoQueueService;

    @ApiOperation(value = "Redis Message Queue Table列表")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping
    public ResponseEntity<Page<InvoiceInfoQueue>> list(InvoiceInfoQueue invoiceInfoQueue, @PathVariable Long organizationId, @ApiIgnore @SortDefault(value = InvoiceInfoQueue.FIELD_ID,
            direction = Sort.Direction.DESC) PageRequest pageRequest) {
        Page<InvoiceInfoQueue> list = invoiceInfoQueueService.selectList(pageRequest, invoiceInfoQueue);
        return Results.success(list);
    }

    @ApiOperation(value = "Redis Message Queue Table明细")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{id}/detail")
    public ResponseEntity<InvoiceInfoQueue> detail(@PathVariable Long id, @PathVariable String organizationId) {
        InvoiceInfoQueue invoiceInfoQueue = invoiceInfoQueueRepository.selectByPrimary(id);
        return Results.success(invoiceInfoQueue);
    }

    @ApiOperation(value = "创建或更新Redis Message Queue Table")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping
    public ResponseEntity<List<InvoiceInfoQueue>> save(@PathVariable Long organizationId, @RequestBody List<InvoiceInfoQueue> invoiceInfoQueues) {
        validObject(invoiceInfoQueues);
        SecurityTokenHelper.validTokenIgnoreInsert(invoiceInfoQueues);
        invoiceInfoQueues.forEach(item -> item.setTenantId(organizationId));
        invoiceInfoQueueService.saveData(invoiceInfoQueues);
        return Results.success(invoiceInfoQueues);
    }

    @ApiOperation(value = "删除Redis Message Queue Table")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @DeleteMapping
    public ResponseEntity<?> remove(@RequestBody List<InvoiceInfoQueue> invoiceInfoQueues) {
        SecurityTokenHelper.validToken(invoiceInfoQueues);
        invoiceInfoQueueRepository.batchDeleteByPrimaryKey(invoiceInfoQueues);
        return Results.success();
    }

}

