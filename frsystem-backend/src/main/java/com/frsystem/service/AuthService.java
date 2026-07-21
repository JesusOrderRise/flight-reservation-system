package com.frsystem.service;


import com.frsystem.dto.auth.LoginRequest;
import com.frsystem.dto.auth.LoginResponse;
import com.frsystem.dto.auth.RegisterRequest;
import com.frsystem.dto.auth.RegisterResponse;
import com.frsystem.enums.UserRoles;
import com.frsystem.exception.ConflictException;
import com.frsystem.exception.UnauthorizedException;
import com.frsystem.mapper.AuthMapper;
import com.frsystem.model.User;
import com.frsystem.repository.UserRepository;
import com.frsystem.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthMapper authMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;


    public RegisterResponse registerPassenger(@Valid RegisterRequest request) {
        User passengerForRegister = authMapper.toUser(request);
        passengerForRegister.setPasswordHash(passwordEncoder.encode(request.getPassword()));


        if (userRepository.findByEmail(passengerForRegister.getEmail()).isPresent()) {
            throw new ConflictException("There is an existing passenger with the same email!");
        }

        passengerForRegister.setRole(UserRoles.PASSENGER);

        return authMapper.toRegisterResponse(userRepository.save(passengerForRegister));
    }

    public RegisterResponse registerAdmin(@Valid RegisterRequest request) {
        User adminForRegister = authMapper.toUser(request);
        adminForRegister.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        if (userRepository.findByEmail(adminForRegister.getEmail()).isPresent()) {
            throw new ConflictException("There is an existing admin with the same email!");
        }

        adminForRegister.setRole(UserRoles.ADMIN);

        return authMapper.toRegisterResponse(userRepository.save(adminForRegister));
    }

    public LoginResponse login(@Valid LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("User not found!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid password!");
        }


        LoginResponse response = authMapper.toLoginResponse(user);
        response.setToken(jwtTokenProvider.generateToken(user));

        return response;
    }

}
