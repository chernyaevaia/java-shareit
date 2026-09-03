package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    void create_withDuplicateEmail_shouldThrowConflict() {
        UserDto dto1 = new UserDto();
        dto1.setName("John");
        dto1.setEmail("duplicate@example.com");
        userService.create(dto1);

        UserDto dto2 = new UserDto();
        dto2.setName("Jane");
        dto2.setEmail("duplicate@example.com");

        assertThatThrownBy(() -> userService.create(dto2))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void create_withInvalidEmail_shouldThrowValidation() {
        UserDto dto = new UserDto();
        dto.setName("John");
        dto.setEmail("invalid-email");

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(ValidationException.class);
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

    @Test
    void getById_nonExistentUser_shouldThrowNotFound() {
        assertThatThrownBy(() -> userService.getById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_shouldUpdateUser() {
        UserDto dto = new UserDto();
        dto.setName("Original");
        dto.setEmail("original@example.com");
        UserDto saved = userService.create(dto);

        UserDto updates = new UserDto();
        updates.setName("Updated");
        updates.setEmail("updated@example.com");

        UserDto updated = userService.update(saved.getId(), updates);

        assertThat(updated.getName()).isEqualTo("Updated");
        assertThat(updated.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void update_withPartialData_shouldUpdateOnlyProvidedFields() {
        UserDto dto = new UserDto();
        dto.setName("Original Name");
        dto.setEmail("original2@example.com");
        UserDto saved = userService.create(dto);

        UserDto updates = new UserDto();
        updates.setName("Updated Name");
        // email is null, should not be updated

        UserDto updated = userService.update(saved.getId(), updates);

        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getEmail()).isEqualTo("original2@example.com");
    }

    @Test
    void getAll_shouldReturnAllUsers() {
        UserDto dto1 = new UserDto();
        dto1.setName("User1");
        dto1.setEmail("user1@example.com");
        userService.create(dto1);

        UserDto dto2 = new UserDto();
        dto2.setName("User2");
        dto2.setEmail("user2@example.com");
        userService.create(dto2);

        List<UserDto> users = userService.getAll();

        assertThat(users).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void delete_shouldRemoveUser() {
        UserDto dto = new UserDto();
        dto.setName("ToDelete");
        dto.setEmail("todelete@example.com");
        UserDto saved = userService.create(dto);

        userService.delete(saved.getId());

        assertThatThrownBy(() -> userService.getById(saved.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_nonExistentUser_shouldThrowNotFound() {
        assertThatThrownBy(() -> userService.delete(999L))
                .isInstanceOf(NotFoundException.class);
    }
}