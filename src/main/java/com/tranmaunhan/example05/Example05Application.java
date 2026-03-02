package com.tranmaunhan.example05;

import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.tranmaunhan.example05.entities.Role;
import com.tranmaunhan.example05.repository.RoleRepo;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@SpringBootApplication
@SecurityScheme(name = "E-Commerce Application", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
public class Example05Application implements CommandLineRunner {

    @Autowired
    private RoleRepo roleRepo;

    public static void main(String[] args) {
        SpringApplication.run(Example05Application.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            createRoleIfNotExist("ADMIN");
            createRoleIfNotExist("USER");

            System.out.println("Roles initialized successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createRoleIfNotExist(String roleName) {
        Optional<Role> existing = roleRepo.findByRoleName(roleName);

        if (existing.isEmpty()) {
            Role role = new Role();
            role.setRoleName(roleName);
            roleRepo.save(role);

            System.out.println("Created new role: " + roleName);
        }
    }
}
