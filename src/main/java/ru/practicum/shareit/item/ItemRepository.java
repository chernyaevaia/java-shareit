package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item create(Item item);

    Item update(Item item);

    Optional<Item> getById(Long id);

    List<Item> getAllByOwnerId(Long ownerId);

    List<Item> search(String text);

    boolean existsById(Long id);
}