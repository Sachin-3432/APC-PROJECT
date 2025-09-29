package com.example.config;

import java.util.List;

import com.example.model.User;
import com.example.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin2541@finvista.com}")
    private String adminEmail;

    @Value("${app.admin.password:Admin@4312}")
    private String adminPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        userRepository.findByEmail(adminEmail).ifPresentOrElse(
                user -> System.out.println("Admin user already exists: " + adminEmail),
                () -> {
                    User admin = new User();
                    admin.setEmail(adminEmail);
                    admin.setFullName("Administrator");
                    admin.setPassword(passwordEncoder.encode(adminPassword));
                    admin.setRoles(List.of("ROLE_ADMIN", "ROLE_USER"));
                    admin.setActive(true);
                    userRepository.save(admin);
                    System.out.println("Created default admin user: " + adminEmail
                            + " (default password can be changed via app.admin.password)");
                });
    }
}