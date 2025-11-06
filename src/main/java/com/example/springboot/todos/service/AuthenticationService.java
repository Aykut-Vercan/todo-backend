package com.example.springboot.todos.service;

import com.example.springboot.todos.request.AuthenticationRequest;
import com.example.springboot.todos.request.RegisterRequest;
import com.example.springboot.todos.response.AuthenticationResponse;

public interface AuthenticationService {
    void register(RegisterRequest input) throws Exception;
    AuthenticationResponse login(AuthenticationRequest request);
}
