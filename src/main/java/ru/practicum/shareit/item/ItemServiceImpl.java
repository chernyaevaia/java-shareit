package ru.practicum.shareit.item;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {
    private static final Logger log = LoggerFactory.getLogger(ItemServiceImpl.class);

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info("Creating item for user with id={}", userId);
        User owner = userRepository.getById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        Item created = itemRepository.create(item);
        log.debug("Item created with id={}", created.getId());
        return ItemMapper.toItemDto(created);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Updating item id={} for user id={}", itemId, userId);
        Item existingItem = itemRepository.getById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + itemId));

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new ForbiddenException(
                    "Item with id=" + itemId + " does not belong to user with id=" + userId);
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updated = itemRepository.update(existingItem);
        log.debug("Item id={} updated", itemId);
        return ItemMapper.toItemDto(updated);
    }

    @Override
    public ItemDto getById(Long itemId) {
        log.info("Fetching item with id={}", itemId);
        Item item = itemRepository.getById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + itemId));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllByOwner(Long userId) {
        log.info("Fetching all items for owner id={}", userId);
        userRepository.getById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        return itemRepository.getAllByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        log.info("Searching items by text='{}'", text);
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}