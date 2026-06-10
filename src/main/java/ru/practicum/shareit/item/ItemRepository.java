package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    Item create(Item item);

    Item update(Item item);

    Item getById(Long id);

    List<Item> getAllByOwnerId(Long ownerId);

    List<Item> search(String text);

    boolean existsById(Long id);
}