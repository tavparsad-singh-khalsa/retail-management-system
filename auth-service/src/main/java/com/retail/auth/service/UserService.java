package com.retail.auth.service;

import com.retail.auth.dto.RegisterResponse;
import com.retail.auth.entity.Role;
import com.retail.auth.entity.User;
import com.retail.auth.exception.EmailAlreadyExistsException;
import com.retail.auth.exception.PhoneAlreadyExistsException;
import com.retail.auth.exception.RoleNotFoundException;
import com.retail.auth.exception.UsernameAlreadyExistsException;
import com.retail.auth.repository.RoleRepository;
import com.retail.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.retail.auth.dto.RegisterRequest;



@Service
@RequiredArgsConstructor

public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private void validateNewUser(RegisterRequest request){
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())){
            throw new EmailAlreadyExistsException("Email already exists");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new PhoneAlreadyExistsException("Phone already exists");
        }
    }

    public RegisterResponse registerUser(RegisterRequest request) {

        validateNewUser(request);

        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() ->
                new RoleNotFoundException("Role not found: " + request.getRole()));

        User user = new User();

        user.setFullName(request.getFullName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(role);
        user.setIsActive(true);
        user.setFailedLoginAttempts(0);
        user.setForcePasswordChange(true);
        user.setAccountLocked(false);
        user.setCreatedBy("SYSTEM");

        User savedUser = userRepository.save(user);

        return mapToRegisterResponse(savedUser);
    }

    private RegisterResponse mapToRegisterResponse(User user) {

        RegisterResponse response = new RegisterResponse();

        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().getName());
        response.setMessage("User registered successfully.");

        return response;
    }
}
