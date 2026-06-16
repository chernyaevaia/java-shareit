package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User create(User user);

    User update(User user);

    Optional<User> getById(Long id);

    List<User> getAll();

    void delete(Long id);

    boolean existsByEmail(String email, Long excludeId);
}