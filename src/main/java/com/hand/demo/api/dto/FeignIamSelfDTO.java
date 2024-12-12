package com.hand.demo.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class FeignIamSelfDTO implements Serializable {
    private String employeeId;
}
