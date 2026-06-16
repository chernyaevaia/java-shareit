package ru.practicum.shareit.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto create(UserDto userDto) {
        log.info("Creating user with email={}", userDto.getEmail());
        validateEmail(userDto.getEmail(), null);
        User user = userRepository.create(UserMapper.toUser(userDto));
        log.debug("User created with id={}", user.getId());
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        log.info("Updating user with id={}", id);
        User existingUser = userRepository.getById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        if (userDto.getEmail() != null && !userDto.getEmail().equals(existingUser.getEmail())) {
            validateEmail(userDto.getEmail(), id);
            existingUser.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            existingUser.setName(userDto.getName());
        }

        User updated = userRepository.update(existingUser);
        log.debug("User id={} updated", id);
        return UserMapper.toUserDto(updated);
    }

    @Override
    public UserDto getById(Long id) {
        log.info("Fetching user with id={}", id);
        User user = userRepository.getById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        log.info("Fetching all users");
        return userRepository.getAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting user with id={}", id);
        userRepository.getById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        userRepository.delete(id);
        log.debug("User id={} deleted", id);
    }

    private void validateEmail(String email, Long excludeId) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new ValidationException("Invalid email format");
        }
        if (userRepository.existsByEmail(email, excludeId)) {
            throw new ConflictException("Email already exists: " + email);
        }
    }
}