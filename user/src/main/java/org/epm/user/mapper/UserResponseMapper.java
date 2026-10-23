package org.epm.user.mapper;

import org.epm.user.dto.UserResponseDto;
import org.epm.user.user.User;
import org.springframework.stereotype.Component;

@Component
public class UserResponseMapper {
    public UserResponseDto toDTO(User user){
        if(user == null) return null;

        return new UserResponseDto(
                user.getId(),
                user.getExternalUserId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getAge(),
                user.getGender()
        );
    }
}
