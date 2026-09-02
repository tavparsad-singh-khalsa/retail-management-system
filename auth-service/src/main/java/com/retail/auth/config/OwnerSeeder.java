package com.retail.auth.config;

import com.retail.auth.entity.Role;
import com.retail.auth.entity.User;
import com.retail.auth.repository.RoleRepository;
import com.retail.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(2)
public class OwnerSeeder implements CommandLineRunner{

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${owner.default.username:owner}")
    private String defaultOwnerUsername;

    @Value("${owner.default.email:}")
    private String defaultOwnerEmail;

    @Value("${owner.default.phone:}")
    private String defaultOwnerPhone;

    @Value("${owner.default.full-name:Default Owner}")
    private String defaultOwnerFullName;

    @Value("${owner.default.password}")
    private String defaultOwnerPassword;

    @Override
    public void run(String... args) {
        createOwnerIfNotExists();
    }

    private void createOwnerIfNotExists() {
        Role ownerRole = roleRepository.findByName("OWNER")
                .orElseThrow(() ->
                    new IllegalStateException("OWNER role not found. Please run RoleSeeder first."));


        if(userRepository.existsByRole(ownerRole)){
            return;

        }


        User owner = new User();

        owner.setUsername(defaultOwnerUsername);
        owner.setPassword(passwordEncoder.encode(defaultOwnerPassword));

        owner.setFullName(defaultOwnerFullName);
        owner.setEmail(defaultOwnerEmail);
        owner.setPhone(defaultOwnerPhone);

        owner.setRole(ownerRole);

        owner.setIsActive(true);
        owner.setForcePasswordChange(true);

        owner.setFailedLoginAttempts(0);
        owner.setAccountLocked(false);

        owner.setCreatedBy("SYSTEM");

        userRepository.save(owner);

        System.out.println("Default OWNER account created successfully. Change its password on first login.");

    }


}
