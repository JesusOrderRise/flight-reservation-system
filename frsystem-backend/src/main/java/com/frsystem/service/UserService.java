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
        User userForRegister = userMapper.toUser(request);
        userForRegister.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userForRegister.setRole(UserRoles.PASSENGER);

        if (userRepository.findByEmail(userForRegister.getEmail()).isPresent()) {
            throw new RuntimeException("There is an existing passenger with the same email!");
        }

        return userMapper.toRegisterResponse(userRepository.save(userForRegister));
    }


}
