package com.retail.auth.controller;

import com.retail.auth.dto.LoginRequest;
import com.retail.auth.dto.LoginResponse;
import com.retail.auth.dto.RegisterRequest;
import com.retail.auth.dto.RegisterResponse;
import com.retail.auth.service.AuthService;
import com.retail.auth.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@Valid @RequestBody RegisterRequest request){

        RegisterResponse response = userService.registerUser(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){

        LoginResponse response = authService.login((request));

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


}
