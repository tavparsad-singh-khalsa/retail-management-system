package com.retail.auth.dto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class RegisterResponse {

    private Long id;

    private String username;

    private String fullName;

    private String email;

    private String role;

    private String message;
}
