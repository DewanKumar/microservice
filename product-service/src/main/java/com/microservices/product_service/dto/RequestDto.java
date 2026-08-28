package com.microservices.product_service.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RequestDto {

    private String name;
    private Double price;
    private String description;
}
