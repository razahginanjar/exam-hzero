package com.hand.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import io.choerodon.resource.annoation.EnableChoerodonResourceServer;

@EnableChoerodonResourceServer
@EnableDiscoveryClient
@SpringBootApplication
//@MapperScan({"com.hand.demo.infra.mapper", "org.hzero.boot.admin.translate.mapper",
//        "org.hzero.boot.imported.infra.mapper"})
@MapperScan({"com.hand.demo.infra.mapper"})
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }


}




