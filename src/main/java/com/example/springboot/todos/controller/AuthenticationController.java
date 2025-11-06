package com.example.springboot.todos.controller;

import com.example.springboot.todos.request.AuthenticationRequest;
import com.example.springboot.todos.request.RegisterRequest;
import com.example.springboot.todos.response.AuthenticationResponse;
import com.example.springboot.todos.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication Rest API endpoints", description = "Operations related to register&login")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Operation(summary = "Register a user", description = "Create new User in database")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/register")
    public void register(@Valid @RequestBody RegisterRequest registerRequest) throws Exception {
        authenticationService.register(registerRequest);
    }
    @Operation(summary = "Login a user", description = "Submit email & password to authenticate user")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/login")
    public AuthenticationResponse login(@Valid @RequestBody AuthenticationRequest authRequest) throws Exception{
        return authenticationService.login(authRequest);
    }

}
