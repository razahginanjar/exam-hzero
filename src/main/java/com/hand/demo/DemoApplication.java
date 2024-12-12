package com.hand.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import io.choerodon.resource.annoation.EnableChoerodonResourceServer;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableChoerodonResourceServer
@EnableDiscoveryClient
@SpringBootApplication
//@MapperScan({"com.hand.demo.infra.mapper", "org.hzero.boot.admin.translate.mapper",
//        "org.hzero.boot.imported.infra.mapper"})
@MapperScan({"com.hand.demo.infra.mapper"})
@EnableFeignClients(basePackages = "com.hand.demo.infra.feign")
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }


}




