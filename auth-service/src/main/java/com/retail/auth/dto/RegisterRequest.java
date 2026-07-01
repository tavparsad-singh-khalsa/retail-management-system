package com.retail.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class RegisterRequest {

    @NotBlank(message = "FullName cannot be empty")
    private String fullName;

    @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Enter valid Email")
    private String email;

    @NotBlank(message = "Phone No can't be empty")
    @Pattern(regexp = "^\\d{10}$",
            message = "Phone number must contain exactly 10 digits")
    private String phone;

    @NotBlank(message = "Password cannot be empty")
    @Size (min = 8 , max = 100 , message = "Password must be at least 8 character")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#]).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character")
    private String password;

    @NotBlank(message = "Role Can't Be empty ")
    private String role;
}
