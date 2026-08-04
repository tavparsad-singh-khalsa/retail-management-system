package com.retail.auth.service;


import com.retail.auth.exception.AccountDisabledException;
import com.retail.auth.exception.AccountLockedException;
import com.retail.auth.exception.InvalidCredentialsException;
import com.retail.auth.security.CustomUserDetails;
import com.retail.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.retail.auth.entity.User;
import com.retail.auth.repository.UserRepository;
import com.retail.auth.dto.LoginRequest;
import com.retail.auth.dto.LoginResponse;

import java.time.Duration;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSecurityService
            userSecurityService;

    private final JwtService jwtService;
    //userRepository.findByUsernameWithRole(...)
    private User getUserByUsername(
            LoginRequest request){
        return userRepository
                .findByUsernameWithRole(request.getUsername())
                .orElseThrow(
                        () -> new InvalidCredentialsException(
                                "Invalid username or password"
                        )
                );
    }
    public LoginResponse login(LoginRequest request){

        User user = getUserByUsername(request);
        String roleName = user.getRole().getName();

        if(!user.getIsActive()){
            throw new AccountDisabledException(
                    "Account is disabled. Please contact administrator."
            );
        }
        if (user.getAccountLocked()) {

            LocalDateTime currentTime = LocalDateTime.now();

            if (!currentTime.isBefore(user.getAccountLockedUntil())) {

                user = userSecurityService.unlockAccount(user);

            } else {

                long minutes =
                        Duration.between(
                                currentTime,
                                user.getAccountLockedUntil()
                        ).toMinutes();

                throw new AccountLockedException(
                        "Account is locked. Please try again in "
                                + minutes +
                                " minutes."
                );
            }
        }
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            userSecurityService.recordFailedLogin(
                    user
            );

            throw new InvalidCredentialsException(
                    "Invalid username or password"
            );
        }
        userSecurityService.recordSuccessfulLogin(user);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        String token = jwtService.generateToken(userDetails);

        return mapToLoginResponse(user, roleName ,token);
    }

    private LoginResponse mapToLoginResponse(
            User user,
            String roleName,
            String token
    ) {
        LoginResponse response = new LoginResponse();

        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setRole(roleName);
        response.setMessage(
                "User logged in successfully."
        );

        return response;
    }
}
