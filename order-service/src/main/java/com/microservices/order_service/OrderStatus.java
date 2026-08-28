package com.microservices.order_service;

public enum OrderStatus {

    CREATED,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    PAYMENT_PENDING
}
