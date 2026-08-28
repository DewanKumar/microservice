package com.microservices.user_service.service;

import com.microservices.user_service.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
//@Service
public interface UserService {
    User createUser(User user);

    List<User> getAllUsers();

    User getUserById(Integer id);

    User updateUser(Integer id, User user);

    void deleteUser(Integer id);
}
