package org.epm.user.mapper;

import org.epm.user.dto.UserCreateDto;
import org.epm.user.user.User;
import org.springframework.stereotype.Component;

@Component
public class UserCreateMapper {
    public UserCreateDto toDTO(User user){
        if(user == null) return null;

        return new UserCreateDto(
                user.getUsername(),
                user.getPassword(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getAge(),
                user.getGender()
        );
    }

    public User toEntity(UserCreateDto userCreateDto){
        if(userCreateDto == null) return null;

        User user = new User();
        user.setUsername(userCreateDto.username());
        user.setPassword(userCreateDto.password());
        user.setFirstName(userCreateDto.firstName());
        user.setLastName(userCreateDto.lastName());
        user.setEmail(userCreateDto.email());
        user.setPhoneNumber(userCreateDto.phoneNumber());
        user.setAge(userCreateDto.age());
        user.setGender(userCreateDto.gender());

        return user;
    }
}
