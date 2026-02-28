package com.app.fooddash.config;

import com.app.fooddash.entity.Role;
import com.app.fooddash.enums.RoleType;
import com.app.fooddash.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        for (RoleType roleType : RoleType.values()) {

            roleRepository.findByName(roleType)
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName(roleType);
                        return roleRepository.save(role);
                    });
        }

        System.out.println("Roles seeded successfully.");
    }
}
