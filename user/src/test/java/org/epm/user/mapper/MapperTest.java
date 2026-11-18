package org.epm.user.mapper;

import org.epm.user.TestData;
import org.epm.user.dto.UserCreateDto;
import org.epm.user.dto.UserResponseDto;
import org.epm.user.user.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

    private final UserCreateMapper createMapper = new UserCreateMapper();
    private final UserResponseMapper responseMapper = new UserResponseMapper();

    @Test
    void createDtoBecomesAnEntityWithAllFields() {
        User user = createMapper.toEntity(new UserCreateDto(
                "ola", "secret", "Ola", "Nordmann", "ola@example.com", "12345678", 30, "Male"));

        assertThat(user.getUsername()).isEqualTo("ola");
        assertThat(user.getPassword()).isEqualTo("secret");
        assertThat(user.getFirstName()).isEqualTo("Ola");
        assertThat(user.getLastName()).isEqualTo("Nordmann");
        assertThat(user.getEmail()).isEqualTo("ola@example.com");
        assertThat(user.getPhoneNumber()).isEqualTo("12345678");
        assertThat(user.getAge()).isEqualTo(30);
        assertThat(user.getGender()).isEqualTo("Male");
        assertThat(user.getId()).isNull();
    }

    @Test
    void entityBecomesACreateDto() {
        User source = TestData.user(1L, UUID.randomUUID(), "ola");

        UserCreateDto dto = createMapper.toDTO(source);

        assertThat(dto.username()).isEqualTo("ola");
        assertThat(dto.password()).isEqualTo("stored-hash");
        assertThat(dto.firstName()).isEqualTo("Ola");
        assertThat(dto.lastName()).isEqualTo("Nordmann");
        assertThat(dto.email()).isEqualTo("ola@example.com");
        assertThat(dto.phoneNumber()).isEqualTo("12345678");
        assertThat(dto.age()).isEqualTo(30);
        assertThat(dto.gender()).isEqualTo("Male");
    }

    @Test
    void createMapperPassesNullThrough() {
        assertThat(createMapper.toEntity(null)).isNull();
        assertThat(createMapper.toDTO(null)).isNull();
    }

    @Test
    void responseDtoNeverContainsThePassword() {
        UUID external = UUID.randomUUID();

        UserResponseDto dto = responseMapper.toDTO(TestData.user(5L, external, "ola"));

        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.externalUserId()).isEqualTo(external);
        assertThat(dto.username()).isEqualTo("ola");
        assertThat(dto.toString()).doesNotContain("stored-hash");
    }

    @Test
    void responseMapperPassesNullThrough() {
        assertThat(responseMapper.toDTO(null)).isNull();
    }
}
