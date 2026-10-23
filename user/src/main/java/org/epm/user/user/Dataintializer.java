package org.epm.user.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class Dataintializer {
    @Bean
    CommandLineRunner initDatabase(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            User user1 = new User("12345678", "Nordmann", "Ola", "ola.nordmann@example.com");
            user1.setUsername("ola");
            user1.setPassword(passwordEncoder.encode("password"));
            user1.setAge(30);
            user1.setGender("male");

            User user2 = new User("87654321", "Nordmann", "Kari", "kari.nordmann@example.com");
            user2.setUsername("kari");
            user2.setPassword(passwordEncoder.encode("password"));
            user2.setAge(28);
            user2.setGender("female");

            userRepo.save(user1);
            userRepo.save(user2);

        };
    }
}
