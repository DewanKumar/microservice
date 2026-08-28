package com.microservices.order_service.client;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductResponse {

    private Integer id;
    private String name;
    private Double price;
    private String description;

}