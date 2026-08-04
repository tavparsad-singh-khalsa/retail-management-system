package com.retail.auth.config;

import com.retail.auth.entity.Role;
import com.retail.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(1)
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        createRoleIfNotExists("OWNER");
        createRoleIfNotExists("MANAGER");
        createRoleIfNotExists("CASHIER");
        createRoleIfNotExists("DEVELOPER");
    }

    private void createRoleIfNotExists(String roleName) {

        if (!roleRepository.existsByName(roleName)) {

            Role role = new Role();
            role.setName(roleName);

            roleRepository.save(role);
        }
    }
}