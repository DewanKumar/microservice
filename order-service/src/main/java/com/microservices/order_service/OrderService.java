package com.microservices.order_service;

import java.util.List;

public interface OrderService {
    Order createOrder(Order order);

    List<Order> getAllOrders();

    Order getOrderById(Integer id);
}
