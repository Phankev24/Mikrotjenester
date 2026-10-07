package org.epm.user.user;

import org.epm.user.event.EventDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {


    private final UserRepo userRepo;
    private final UserClient userClient;

    public UserService(UserRepo userRepo, UserClient userClient) {
        this.userRepo = userRepo;
        this.userClient = userClient;
    }

    public List<User> findAll() {
        List<User> users = userRepo.findAll();
        return userRepo.findAll();
    }

    public User findByUserId(Long id) {
        return userRepo.findById(id).orElse(null);

    }

    public User createUser(User user) {
        return userRepo.save(user);
    }

    public void deleteById(Long id) {
        userRepo.deleteById(id);
    }

    public UserWithEventsDto getUserWithEvents(Long id) {
        User user = findByUserId(id);
        List<EventDto> events = userClient.getAllEvents();
        return new UserWithEventsDto(user, events);
    }
}
