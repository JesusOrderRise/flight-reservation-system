package com.frsystem.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Email(message = "Please provide a valid email address.")
    @NotBlank(message = "Email Name cannot be blank.")
    private String email;


    @NotBlank(message = "Password cannot be empty.")
    private String password;

}
