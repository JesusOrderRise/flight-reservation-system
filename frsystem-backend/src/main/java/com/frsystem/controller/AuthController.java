package com.frsystem.controller;

import com.frsystem.dto.auth.LoginRequest;
import com.frsystem.dto.auth.LoginResponse;
import com.frsystem.dto.auth.RegisterRequest;
import com.frsystem.dto.auth.RegisterResponse;
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

    @PostMapping(path = "/register/passenger")
    public RegisterResponse passengerRegister(@Valid @RequestBody RegisterRequest request) {
        return authService.registerPassenger(request);
    }

    @PostMapping(path = "/register/admin")
    public RegisterResponse adminRegister(@Valid @RequestBody RegisterRequest request) {
        return authService.registerAdmin(request);
    }

    @PostMapping(path = "/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }


}
