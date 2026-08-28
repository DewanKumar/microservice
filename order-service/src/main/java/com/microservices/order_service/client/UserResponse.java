package com.microservices.order_service.client;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserResponse {
    private Integer id;
    private String name;
    private String email;
    private String phone;
}
