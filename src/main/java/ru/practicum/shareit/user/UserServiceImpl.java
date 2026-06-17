package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    @Override
    public UserDto create(UserDto userDto) {
        log.info("Creating user with email={}", userDto.getEmail());
        validateEmail(userDto.getEmail(), null);

        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());

        User saved;
        try {
            saved = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Email already exists: " + userDto.getEmail());
        }
        log.debug("User created with id={}", saved.getId());
        return UserMapper.toUserDto(saved);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        log.info("Updating user with id={}", id);
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        if (userDto.getEmail() != null && !userDto.getEmail().equals(existing.getEmail())) {
            validateEmail(userDto.getEmail(), id);
            existing.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            existing.setName(userDto.getName());
        }

        User saved;
        try {
            saved = userRepository.save(existing);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Email already exists: " + userDto.getEmail());
        }
        log.debug("User id={} updated", id);
        return UserMapper.toUserDto(saved);
    }

    @Override
    public UserDto getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting user with id={}", id);
        userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
        userRepository.deleteById(id);
    }

    private void validateEmail(String email, Long excludeId) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new ValidationException("Invalid email format");
        }
        if (userRepository.existsByEmailExcludingId(email, excludeId)) {
            throw new ConflictException("Email already exists: " + email);
        }
    }
}