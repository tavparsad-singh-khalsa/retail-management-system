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
@Order(3)
public class DevelopmentAdminSeeder implements CommandLineRunner {

    @Value("${dev.admin.username:admin}")
    private String devAdminUsername;

    @Value("${dev.admin.email:admin@retail.local}")
    private String devAdminEmail;

    @Value("${dev.admin.phone:9999999999}")
    private String devAdminPhone;

    @Value("${dev.admin.full-name:Development Admin}")
    private String devAdminFullName;

    @Value("${dev.admin.password}")
    private String devAdminPassword;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createDevAdminIfNotExists();
    }

    private void createDevAdminIfNotExists() {
        if (userRepository.existsByUsername(devAdminUsername)) {
            return;
        }

        Role ownerRole = roleRepository.findByName("OWNER")
                .orElseThrow(() ->
                        new IllegalStateException("OWNER role not found. Please run RoleSeeder first."));

        User admin = new User();
        admin.setUsername(devAdminUsername);
        admin.setPassword(passwordEncoder.encode(devAdminPassword));
        admin.setFullName(devAdminFullName);
        admin.setEmail(devAdminEmail);
        admin.setPhone(devAdminPhone);
        admin.setRole(ownerRole);
        admin.setIsActive(true);
        admin.setForcePasswordChange(false);
        admin.setFailedLoginAttempts(0);
        admin.setAccountLocked(false);
        admin.setCreatedBy("SYSTEM");

        userRepository.save(admin);

        System.out.println("Development ADMIN account created successfully.");
    }
}
