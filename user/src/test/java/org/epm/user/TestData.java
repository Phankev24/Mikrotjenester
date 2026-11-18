package org.epm.user;

import org.epm.user.user.User;

import java.util.UUID;

public final class TestData {

    private TestData() {
    }

    public static User user(Long id, UUID externalUserId, String username) {
        User user = new User("12345678", "Nordmann", "Ola", "ola@example.com");
        user.setId(id);
        user.setExternalUserId(externalUserId);
        user.setUsername(username);
        user.setPassword("stored-hash");
        user.setAge(30);
        user.setGender("Male");
        return user;
    }
}
