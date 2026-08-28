package com.microservices.order_service;


import com.microservices.order_service.client.ProductClient;
import com.microservices.order_service.client.ProductResponse;
import com.microservices.order_service.client.UserClient;
import com.microservices.order_service.client.UserResponse;
import com.microservices.order_service.kafka.OrderCreatedEvent;
import com.microservices.order_service.kafka.OrderEventProducer;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final OrderEventProducer orderEventProducer;

    @Override
    public Order createOrder(Order order) {

        // Check user
        UserResponse user =
                userClient.getUserById(order.getUserId());

        // Get product
        ProductResponse product =
                productClient.getProductById(order.getProductId());

        // Calculate total price
        double totalPrice =
                product.getPrice() * order.getQuantity();

        order.setTotalPrice(totalPrice);

        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.PAYMENT_PENDING);
        }

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Create Kafka event
        OrderCreatedEvent event =
                new OrderCreatedEvent(
                        savedOrder.getId(),
                        savedOrder.getUserId(),
                        savedOrder.getProductId(),
                        savedOrder.getQuantity(),
                        savedOrder.getTotalPrice()
                );

        // Publish event
        orderEventProducer.publishOrderCreated(event);

        return savedOrder;
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order getOrderById(Integer id) {

        return orderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found with id: " + id
                        ));
    }
}