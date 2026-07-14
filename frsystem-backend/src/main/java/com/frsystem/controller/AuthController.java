package com.frsystem.controller;

import com.frsystem.dto.user.LoginRequest;
import com.frsystem.dto.user.LoginResponse;
import com.frsystem.dto.user.RegisterRequest;
import com.frsystem.dto.user.RegisterResponse;
import com.frsystem.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping(path = "/register")
    public RegisterResponse passengerRegister(@Valid @RequestBody RegisterRequest request) {
        return authService.registerPassenger(request);
    }

    @PostMapping(path = "/admin-register")
    public RegisterResponse adminRegister(@Valid @RequestBody RegisterRequest request) {
        return authService.registerAdmin(request);
    }

    @PostMapping(path = "/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }


}
