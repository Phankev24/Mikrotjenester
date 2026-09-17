package org.epm.user.user;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {


    private final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public List<User> findAll() {
        List<User> users = userRepo.findAll();
        return users;
    }
}
