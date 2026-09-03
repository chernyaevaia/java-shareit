package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplIntegrationTest {

    private final UserService userService;

    @Test
    void create_shouldPersistUser() {
        UserDto dto = new UserDto();
        dto.setName("John");
        dto.setEmail("john@example.com");

        UserDto saved = userService.create(dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("John");
        assertThat(saved.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getById_shouldReturnPersistedUser() {
        UserDto dto = new UserDto();
        dto.setName("Jane");
        dto.setEmail("jane@example.com");
        UserDto saved = userService.create(dto);

        UserDto found = userService.getById(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getName()).isEqualTo("Jane");
        assertThat(found.getEmail()).isEqualTo("jane@example.com");
    }
}