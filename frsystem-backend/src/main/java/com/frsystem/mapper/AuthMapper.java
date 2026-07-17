package com.frsystem.mapper;

import com.frsystem.dto.auth.LoginRequest;
import com.frsystem.dto.auth.LoginResponse;
import com.frsystem.dto.auth.RegisterRequest;
import com.frsystem.dto.auth.RegisterResponse;
import com.frsystem.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toUser(RegisterRequest request);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    User toUser(LoginRequest request);


    RegisterResponse toRegisterResponse(User user);

    @Mapping(target = "token", ignore = true)
    LoginResponse toLoginResponse(User user);

}
