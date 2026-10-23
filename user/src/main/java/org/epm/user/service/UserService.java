package org.epm.user.service;

import org.epm.user.client.UserClient;
import org.epm.user.dto.UserCreateDto;
import org.epm.user.dto.UserResponseDto;
import org.epm.user.dto.UserEventCreateDto;
import org.epm.user.dto.UserUpdateDto;
import org.epm.user.dto.UserWithEventsDto;
import org.epm.user.event.EventCreateDto;
import org.epm.user.event.EventDto;
import org.epm.user.mapper.UserCreateMapper;
import org.epm.user.mapper.UserResponseMapper;
import org.epm.user.user.User;
import org.epm.user.user.UserRepo;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {


    private final UserRepo userRepo;
    private final UserClient userClient;
    private final UserResponseMapper userResponseMapper;
    private final UserCreateMapper userCreateMapper;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepo userRepo, UserClient userClient, UserResponseMapper userResponseMapper, UserCreateMapper userCreateMapper, PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.userRepo = userRepo;
        this.userClient = userClient;
        this.userResponseMapper = userResponseMapper;
        this.userCreateMapper = userCreateMapper;
    }

    public List<UserResponseDto> findAll() {
        return userRepo.findAll()
                .stream()
                .map(userResponseMapper::toDTO)
                .toList();
    }

    public UserResponseDto findByUserId(Long id) {
        return userRepo.findById(id)
                .map(userResponseMapper::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with this id: " + id));
    }

    public UserResponseDto findByExternalUserId(UUID externalUserId) {
        return userRepo.findByExternalUserId(externalUserId)
                .map(userResponseMapper::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with this external id: " + externalUserId));
    }

    public UserResponseDto createUser(UserCreateDto userCreateDto) {
        User userEntity = userCreateMapper.toEntity(userCreateDto);
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        User savedUser = userRepo.save(userEntity);
        return userResponseMapper.toDTO(savedUser);
    }

    public void deleteById(Long id) {
        if (!userRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with this id: " + id);
        }
        userRepo.deleteById(id);
    }

    public UserWithEventsDto getUserWithEvents(Long id) {
        UserResponseDto user = findByUserId(id);
        List<EventDto> events = userClient.getAllEvents()
                .stream()
                .filter(event -> user.externalUserId().equals(event.externalUserId()))
                .toList();
        return new UserWithEventsDto(user, events);
    }

    public EventDto createEventForUser(UUID externalUserId, UserEventCreateDto dto) {
        UserResponseDto user = findByExternalUserId(externalUserId);
        EventCreateDto eventCreateDto = new EventCreateDto(
                dto.eventName(),
                dto.eventDescription(),
                dto.eventCategory(),
                dto.eventLocation(),
                dto.eventAttendance()
        );
        return userClient.createEvent(user.externalUserId(), eventCreateDto);
    }

    public UserResponseDto updatedUser(Long id, UserUpdateDto userUpdateDto){
        User user = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with this id: " + id));

        if(userUpdateDto.username() != null){
            user.setUsername(userUpdateDto.username());
        }

        if(userUpdateDto.password() != null){
            user.setPassword(passwordEncoder.encode(userUpdateDto.password()));
        }

        if(userUpdateDto.age() != null){
            user.setAge(userUpdateDto.age());
        }

        if(userUpdateDto.gender() != null){
            user.setGender(userUpdateDto.gender());
        }

        if(userUpdateDto.firstName() != null){
            user.setFirstName(userUpdateDto.firstName());
        }

        if(userUpdateDto.lastName() != null){
            user.setLastName(userUpdateDto.lastName());
        }

        if(userUpdateDto.email() != null){
            user.setEmail(userUpdateDto.email());
        }

        if(userUpdateDto.phoneNumber() != null){
            user.setPhoneNumber(userUpdateDto.phoneNumber());
        }

        User updatedUser = userRepo.save(user);

        return userResponseMapper.toDTO(updatedUser);
    }
}
