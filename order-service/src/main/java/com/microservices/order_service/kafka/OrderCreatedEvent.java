package com.microservices.order_service.kafka;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    private Integer orderId;
    private Integer userId;
    private Integer productId;
    private Integer quantity;
    private Double totalPrice;


}