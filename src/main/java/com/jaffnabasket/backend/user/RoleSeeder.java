package com.jaffnabasket.backend.user;

import com.jaffnabasket.backend.user.entity.Role;
import com.jaffnabasket.backend.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements ApplicationRunner {

    private static final List<String> DEFAULT_ROLES = List.of(
            "CUSTOMER",
            "SUPER_ADMIN",
            "COMMERCE_MANAGER",
            "CATALOG_MANAGER",
            "WAREHOUSE_OPERATOR",
            "CUSTOMER_SUPPORT",
            "FINANCE_AUDITOR",
            "MARKETING_MANAGER"
    );

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String roleName : DEFAULT_ROLES) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(Role.builder().name(roleName).description(roleName + " role").build());
            }
        }
    }
}
