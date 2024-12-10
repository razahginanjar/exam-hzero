package com.hand.demo.infra.feign.fallback;

import com.hand.demo.infra.feign.DemoFeign;
import org.hzero.boot.interfaces.sdk.invoke.InterfaceInvokeSdk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * DemoFeignFallBack
 */
@Component
public class DemoFeignFallBack implements DemoFeign {
    @Autowired
    InterfaceInvokeSdk interfaceInvokeSdk;
}
