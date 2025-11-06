package com.example.springboot.todos.service;

import com.example.springboot.todos.response.UserResponse;

import java.util.List;

public interface AdminService {
    List<UserResponse> getAllUsers();
    UserResponse promoteToAdmin(long userId);
    void deleteNonAdmin(long userId);

}
