package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void toUserDto_shouldMapAllFields() {
        User user = new User(1L, "John", "john@mail.com");

        UserDto dto = UserMapper.toUserDto(user);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("John");
        assertThat(dto.getEmail()).isEqualTo("john@mail.com");
    }

    @Test
    void toUser_shouldMapAllFields() {
        UserDto dto = new UserDto(1L, "John", "john@mail.com");

        User user = UserMapper.toUser(dto);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("John");
        assertThat(user.getEmail()).isEqualTo("john@mail.com");
    }
}