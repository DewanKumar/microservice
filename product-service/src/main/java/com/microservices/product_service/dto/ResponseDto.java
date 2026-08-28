package com.microservices.product_service.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResponseDto {
    private Integer id;

    private String name;
    private Double price;
    private String description;
}
