package com.microservices.user_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDto {

    private String name;
    private String email;
    private String phone;
}
