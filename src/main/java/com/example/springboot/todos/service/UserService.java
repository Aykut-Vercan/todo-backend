package com.example.springboot.todos.service;

import com.example.springboot.todos.request.PasswordUpdateRequest;
import com.example.springboot.todos.response.UserResponse;

public interface UserService {
    UserResponse getUserInfo();
    void deleteUser();
    void updatePassword(PasswordUpdateRequest passwordUpdateRequest);
}
