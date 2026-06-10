package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto create(UserDto userDto) {
        validateEmail(userDto.getEmail(), null);
        User user = userRepository.create(UserMapper.toUser(userDto));
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User existingUser = userRepository.getById(id);
        
        if (userDto.getEmail() != null && !userDto.getEmail().equals(existingUser.getEmail())) {
            validateEmail(userDto.getEmail(), id);
            existingUser.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        
        User updated = userRepository.update(existingUser);
        return UserMapper.toUserDto(updated);
    }

    @Override
    public UserDto getById(Long id) {
        return UserMapper.toUserDto(userRepository.getById(id));
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.getAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        userRepository.delete(id);
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