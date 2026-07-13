package com.frsystem.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!?*()]).*$",
            message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character (@#$%^&+=!?*())"
    )
    @NotBlank(message = "Password cannot be empty.")
    private String password;

}
