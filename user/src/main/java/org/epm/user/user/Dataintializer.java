package org.epm.user.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Configuration
@Profile("dev")
public class Dataintializer {
    @Bean
    CommandLineRunner initDatabase(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            User user1 = new User("12345678", "Nordmann", "Ola", "ola.nordmann@example.com");
            user1.setExternalUserId(UUID.fromString("a7f3bf47-f373-4cdf-b81c-b0475305b7fe"));
            user1.setUsername("ola");
            user1.setPassword(passwordEncoder.encode("password"));
            user1.setAge(30);
            user1.setGender("male");

            User user2 = new User("87654321", "Nordmann", "Kari", "kari.nordmann@example.com");
            user2.setExternalUserId(UUID.fromString("0b6e2c1a-5d3f-4e8a-9c47-2f1d8a6b7e90"));
            user2.setUsername("kari");
            user2.setPassword(passwordEncoder.encode("password"));
            user2.setAge(28);
            user2.setGender("female");

            userRepo.save(user1);
            userRepo.save(user2);

        };
    }
}
