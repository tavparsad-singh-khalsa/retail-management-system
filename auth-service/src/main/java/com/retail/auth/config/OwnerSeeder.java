package com.retail.auth.config;

import com.retail.auth.entity.Role;
import com.retail.auth.entity.User;
import com.retail.auth.repository.RoleRepository;
import com.retail.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.SQLOutput;
import java.util.Optional;


@Component
@RequiredArgsConstructor
@Order(2)
public class OwnerSeeder implements CommandLineRunner{

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

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

        owner.setUsername("owner");
        owner.setPassword(passwordEncoder.encode("ChangeMe123"));

        owner.setFullName("Gurmeet Singh Saharanpuri");
        owner.setEmail("gurmeetsinghsaharnpuri@gmail.com");
        owner.setPhone("9646131305");

        owner.setRole(ownerRole);

        owner.setIsActive(true);
        owner.setForcePasswordChange(true);

        owner.setFailedLoginAttempts(0);
        owner.setAccountLocked(false);

        owner.setCreatedBy("SYSTEM");

        userRepository.save(owner);

        System.out.println("Default OWNER account created successfully.");

    }


}
