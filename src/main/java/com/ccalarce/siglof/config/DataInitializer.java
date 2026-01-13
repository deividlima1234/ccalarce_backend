package com.ccalarce.siglof.config;

import com.ccalarce.siglof.model.entity.User;
import com.ccalarce.siglof.model.enums.Role;
import com.ccalarce.siglof.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User superAdmin = User.builder()
                    .username("admin")
                    .fullName("Super Administrator")
                    .password(passwordEncoder.encode("admin123")) // Default strong password should be changed
                    .role(Role.SUPER_ADMIN)
                    .active(true)
                    .build();
            userRepository.save(superAdmin);
            System.out.println("Super Admin user seeded: admin / admin123");
        }
    }
}
