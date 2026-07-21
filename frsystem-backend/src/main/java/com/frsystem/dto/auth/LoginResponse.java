package com.frsystem.dto.auth;

import com.frsystem.enums.UserRoles;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private UserRoles role;

}
