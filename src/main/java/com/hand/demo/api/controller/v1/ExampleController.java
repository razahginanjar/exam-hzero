package com.hand.demo.api.controller.v1;

import com.hand.demo.app.service.ExampleService;
import com.hand.demo.domain.repository.ExampleRepository;
import com.hand.demo.infra.util.Utils;
import io.choerodon.core.oauth.DetailsHelper;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.extern.slf4j.Slf4j;
import org.hzero.boot.interfaces.sdk.dto.ResponsePayloadDTO;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import com.hand.demo.config.SwaggerTags;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import java.awt.*;

/**
 * API接口
 */
@Api(tags = SwaggerTags.EXAMPLE)
@RestController("exampleController.v1")
@RequestMapping("/v1/{organizationId}/example")
@Slf4j
public class ExampleController extends BaseController {

    @Autowired
    private Utils utils;
    @Autowired
    private ExampleRepository exampleRepository;

    @ApiOperation(value = "根据ID获取")
    @Permission(level = ResourceLevel.SITE, permissionLogin = true)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ID", paramType = "path")
    })
    @GetMapping()
    public ResponseEntity<?> translated(
            @PathVariable Long organizationId,
            @RequestParam(name = "text", required = false) String text,
            @RequestParam(name = "namespace", required = false) String namespace,
            @RequestParam(name = "interfaceCode", required = false) String interfaceCode,
            @RequestParam(name = "serverCode", required = false) String server,
            HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        log.info(authorization);
        ResponsePayloadDTO authorization1 = utils.invokeTranslation(null,
                namespace, server, interfaceCode, authorization, text);
        return Results.success(authorization1);
    }

    @ApiOperation(value = "根据ID获取")
    @Permission(level = ResourceLevel.SITE, permissionLogin = true)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "ID", paramType = "path")
    })
    @PostMapping(
            consumes = MediaType.TEXT_PLAIN
    )
    public ResponseEntity<?> calculated(
            @PathVariable Long organizationId,
            @RequestParam(name = "namespace", required = false) String namespace,
            @RequestParam(name = "interfaceCode", required = false) String interfaceCode,
            @RequestParam(name = "serverCode", required = false) String server,
            @RequestBody String inputs,
            HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        log.info(authorization);
        ResponsePayloadDTO authorization1 = utils.invokeTranslation(inputs,
                namespace, server, interfaceCode, authorization, null);
        return Results.success(authorization1);
    }


}


