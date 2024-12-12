package com.hand.demo.infra.feign;

import com.hand.demo.api.dto.FeignIamSelfDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "HZERO-IAM", path = "/hzero/v1/")
public interface IamFeign {

    @GetMapping(path = "users/self")
    public ResponseEntity<FeignIamSelfDTO> get();
}
