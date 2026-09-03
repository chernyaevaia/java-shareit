package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    // --- create ---

    @Test
    void create_withValidData_shouldReturnUserDto() {
        UserDto dto = makeUserDto(null, "John", "john@mail.com");
        User saved = makeUser(1L, "John", "john@mail.com");

        when(userRepository.existsByEmailExcludingId("john@mail.com", null)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserDto result = userService.create(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John");
        assertThat(result.getEmail()).isEqualTo("john@mail.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_withNullEmail_shouldThrowValidation() {
        UserDto dto = makeUserDto(null, "John", null);

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid email format");

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_withBlankEmail_shouldThrowValidation() {
        UserDto dto = makeUserDto(null, "John", "   ");

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid email format");

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_withEmptyEmail_shouldThrowValidation() {
        UserDto dto = makeUserDto(null, "John", "");

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid email format");

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_withEmailMissingAt_shouldThrowValidation() {
        UserDto dto = makeUserDto(null, "John", "johnmail.com");

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid email format");

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_withDuplicateEmail_shouldThrowConflict() {
        UserDto dto = makeUserDto(null, "John", "john@mail.com");

        when(userRepository.existsByEmailExcludingId("john@mail.com", null)).thenReturn(true);

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists: john@mail.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_whenDatabaseThrowsConstraintViolation_shouldThrowConflict() {
        UserDto dto = makeUserDto(null, "John", "john@mail.com");

        when(userRepository.existsByEmailExcludingId("john@mail.com", null)).thenReturn(false);
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        assertThatThrownBy(() -> userService.create(dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists: john@mail.com");
    }

    // --- update ---

    @Test
    void update_withNewNameAndEmail_shouldUpdateBoth() {
        User existing = makeUser(1L, "John", "john@mail.com");
        UserDto updates = makeUserDto(null, "John Updated", "johnupdated@mail.com");
        User saved = makeUser(1L, "John Updated", "johnupdated@mail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailExcludingId("johnupdated@mail.com", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserDto result = userService.update(1L, updates);

        assertThat(result.getName()).isEqualTo("John Updated");
        assertThat(result.getEmail()).isEqualTo("johnupdated@mail.com");
    }

    @Test
    void update_withOnlyName_shouldUpdateNameOnly() {
        User existing = makeUser(1L, "John", "john@mail.com");
        UserDto updates = makeUserDto(null, "John Updated", null);
        User saved = makeUser(1L, "John Updated", "john@mail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserDto result = userService.update(1L, updates);

        assertThat(result.getName()).isEqualTo("John Updated");
        assertThat(result.getEmail()).isEqualTo("john@mail.com");
        verify(userRepository, never()).existsByEmailExcludingId(anyString(), any());
    }

    @Test
    void update_withOnlyEmail_shouldUpdateEmailOnly() {
        User existing = makeUser(1L, "John", "john@mail.com");
        UserDto updates = makeUserDto(null, null, "newemail@mail.com");
        User saved = makeUser(1L, "John", "newemail@mail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailExcludingId("newemail@mail.com", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserDto result = userService.update(1L, updates);

        assertThat(result.getName()).isEqualTo("John");
        assertThat(result.getEmail()).isEqualTo("newemail@mail.com");
    }

    @Test
    void update_withSameEmail_shouldSkipEmailValidation() {
        User existing = makeUser(1L, "John", "john@mail.com");
        UserDto updates = makeUserDto(null, "John Updated", "john@mail.com");
        User saved = makeUser(1L, "John Updated", "john@mail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserDto result = userService.update(1L, updates);

        assertThat(result.getName()).isEqualTo("John Updated");
        verify(userRepository, never()).existsByEmailExcludingId(anyString(), any());
    }

    @Test
    void update_withBlankName_shouldNotUpdateName() {
        User existing = makeUser(1L, "John", "john@mail.com");
        UserDto updates = makeUserDto(null, "   ", null);
        User saved = makeUser(1L, "John", "john@mail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserDto result = userService.update(1L, updates);

        assertThat(result.getName()).isEqualTo("John");
    }

    @Test
    void update_withEmptyName_shouldNotUpdateName() {
        User existing = makeUser(1L, "John", "john@mail.com");
        UserDto updates = makeUserDto(null, "", null);
        User saved = makeUser(1L, "John", "john@mail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserDto result = userService.update(1L, updates);

        assertThat(result.getName()).isEqualTo("John");
    }

    @Test
    void update_withNoChanges_shouldReturnUnchanged() {
        User existing = makeUser(1L, "John", "john@mail.com");
        UserDto updates = makeUserDto(null, null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(existing);

        UserDto result = userService.update(1L, updates);

        assertThat(result.getName()).isEqualTo("John");
        assertThat(result.getEmail()).isEqualTo("john@mail.com");
    }

    @Test
    void update_userNotFound_shouldThrowNotFound() {
        UserDto updates = makeUserDto(null, "John", "john@mail.com");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(99L, updates))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found with id: 99");

        verify(userRepository, never()).save(any());
    }

    @Test
    void update_withDuplicateEmail_shouldThrowConflict() {
        User existing = makeUser(1L, "John", "john@mail.com");
        UserDto updates = makeUserDto(null, null, "existing@mail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailExcludingId("existing@mail.com", 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.update(1L, updates))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists: existing@mail.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void update_withInvalidEmailFormat_shouldThrowValidation() {
        User existing = makeUser(1L, "John", "john@mail.com");
        UserDto updates = makeUserDto(null, null, "invalid-email");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> userService.update(1L, updates))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid email format");

        verify(userRepository, never()).save(any());
    }

    @Test
    void update_whenDatabaseThrowsConstraintViolation_shouldThrowConflict() {
        User existing = makeUser(1L, "John", "john@mail.com");
        UserDto updates = makeUserDto(null, null, "newemail@mail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailExcludingId("newemail@mail.com", 1L)).thenReturn(false);
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        assertThatThrownBy(() -> userService.update(1L, updates))
                .isInstanceOf(ConflictException.class);
    }

    // --- getById ---

    @Test
    void getById_existingUser_shouldReturnUserDto() {
        User user = makeUser(1L, "John", "john@mail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John");
        assertThat(result.getEmail()).isEqualTo("john@mail.com");
    }

    @Test
    void getById_userNotFound_shouldThrowNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
    }

    // --- getAll ---

    @Test
    void getAll_withMultipleUsers_shouldReturnAll() {
        User user1 = makeUser(1L, "John", "john@mail.com");
        User user2 = makeUser(2L, "Jane", "jane@mail.com");
        User user3 = makeUser(3L, "Bob", "bob@mail.com");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2, user3));

        List<UserDto> result = userService.getAll();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getName()).isEqualTo("John");
        assertThat(result.get(1).getName()).isEqualTo("Jane");
        assertThat(result.get(2).getName()).isEqualTo("Bob");
    }

    @Test
    void getAll_withNoUsers_shouldReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDto> result = userService.getAll();

        assertThat(result).isEmpty();
    }

    // --- delete ---

    @Test
    void delete_existingUser_shouldDeleteSuccessfully() {
        User user = makeUser(1L, "John", "john@mail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(1L);

        userService.delete(1L);

        verify(userRepository).findById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_userNotFound_shouldThrowNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found with id: 99");

        verify(userRepository, never()).deleteById(any());
    }

    // --- helpers ---

    private User makeUser(Long id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private UserDto makeUserDto(Long id, String name, String email) {
        UserDto dto = new UserDto();
        dto.setId(id);
        dto.setName(name);
        dto.setEmail(email);
        return dto;
    }
}