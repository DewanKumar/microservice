package com.microservices.order_service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseDto {
    private Integer id;
    private Integer userId;
    private Integer productId;
    private Integer quantity;
    private Double totalPrice;
    private OrderStatus status;
}
