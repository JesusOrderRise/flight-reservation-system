package com.frsystem.service;


import com.frsystem.dto.user.RegisterRequest;
import com.frsystem.dto.user.RegisterResponse;
import com.frsystem.enums.UserRoles;
import com.frsystem.mapper.UserMapper;
import com.frsystem.model.User;
import com.frsystem.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public RegisterResponse registerPassenger(@Valid RegisterRequest request) {
        User passengerForRegister = userMapper.toUser(request);
        passengerForRegister.setPasswordHash(passwordEncoder.encode(request.getPassword()));


        if (userRepository.findByEmail(passengerForRegister.getEmail()).isPresent()) {
            throw new RuntimeException("There is an existing passenger with the same email!");
        }

        passengerForRegister.setRole(UserRoles.PASSENGER);

        return userMapper.toRegisterResponse(userRepository.save(passengerForRegister));
    }

    public RegisterResponse registerAdmin(@Valid RegisterRequest request) {
        User adminForRegister = userMapper.toUser(request);
        adminForRegister.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        adminForRegister.setRole(UserRoles.PASSENGER);

        if (userRepository.findByEmail(adminForRegister.getEmail()).isPresent()) {
            throw new RuntimeException("There is an existing admin with the same email!");
        }

        adminForRegister.setRole(UserRoles.ADMIN);

        return userMapper.toRegisterResponse(userRepository.save(adminForRegister));
    }

}
