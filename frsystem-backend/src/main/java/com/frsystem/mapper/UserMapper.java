package com.frsystem.mapper;


import com.frsystem.dto.UserResponse;
import com.frsystem.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
