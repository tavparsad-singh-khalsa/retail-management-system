package com.retail.auth.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class LoginResponse {
    private String token;
    private String username;
    private String role;
    private String message;
}
