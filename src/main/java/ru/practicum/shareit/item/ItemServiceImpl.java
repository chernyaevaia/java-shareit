package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private static final Logger log = LoggerFactory.getLogger(ItemServiceImpl.class);

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        log.info("Creating item for user with id={}", userId);
        User owner = getUserOrThrow(userId);

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);

        Item created = itemRepository.save(item);
        log.debug("Item created with id={}", created.getId());
        return ItemMapper.toItemDto(created);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Updating item id={} for user id={}", itemId, userId);
        Item existing = getItemOrThrow(itemId);

        if (!existing.getOwner().getId().equals(userId)) {
            throw new ForbiddenException(
                    "Item with id=" + itemId + " does not belong to user with id=" + userId);
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existing.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existing.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existing.setAvailable(itemDto.getAvailable());
        }

        Item updated = itemRepository.save(existing);
        return ItemMapper.toItemDto(updated);
    }

    @Override
    public ItemDto getById(Long itemId, Long userId) {
        log.info("Fetching item id={} for user id={}", itemId, userId);
        Item item = getItemOrThrow(itemId);

        ItemDto dto = ItemMapper.toItemDto(item);
        dto.setComments(loadComments(itemId));

        if (item.getOwner().getId().equals(userId)) {
            fillBookingsForOwner(dto, itemId);
        }
        return dto;
    }

    @Override
    public List<ItemDto> getAllByOwner(Long userId) {
        log.info("Fetching all items for owner id={}", userId);
        getUserOrThrow(userId);

        List<Item> items = itemRepository.findAllByOwnerIdOrderByIdAsc(userId);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        List<Booking> allBookings = bookingRepository.findAllByItemIdInAndStatus(itemIds, BookingStatus.APPROVED);
        Map<Long, List<Booking>> bookingsByItem = allBookings.stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        List<Comment> allComments = commentRepository.findAllByItemIdIn(itemIds);
        Map<Long, List<Comment>> commentsByItem = allComments.stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    ItemDto dto = ItemMapper.toItemDto(item);

                    List<Booking> itemBookings = bookingsByItem.getOrDefault(item.getId(), Collections.emptyList());

                    Booking lastBooking = itemBookings.stream()
                            .filter(b -> !b.getStart().isAfter(now))
                            .max(Comparator.comparing(Booking::getStart))
                            .orElse(null);

                    Booking nextBooking = itemBookings.stream()
                            .filter(b -> b.getStart().isAfter(now))
                            .min(Comparator.comparing(Booking::getStart))
                            .orElse(null);

                    if (lastBooking != null) {
                        dto.setLastBooking(BookingMapper.toBookingShortDto(lastBooking));
                    }
                    if (nextBooking != null) {
                        dto.setNextBooking(BookingMapper.toBookingShortDto(nextBooking));
                    }

                    List<Comment> itemComments = commentsByItem.getOrDefault(item.getId(), Collections.emptyList());
                    dto.setComments(itemComments.stream().map(CommentMapper::toDto).collect(Collectors.toList()));

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        log.info("Searching items by text='{}'", text);
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentRequestDto dto) {
        log.info("Adding comment by user id={} to item id={}", userId, itemId);

        User author = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);

        boolean hasPastBooking = bookingRepository
                .existsByBookerIdAndItemIdAndStatusAndEndBefore(
                        userId, itemId, BookingStatus.APPROVED, LocalDateTime.now());

        if (!hasPastBooking) {
            throw new ValidationException(
                    "User id=" + userId + " did not rent item id=" + itemId + " before, comment not allowed");
        }

        Comment comment = CommentMapper.toComment(dto, item, author);

        Comment saved = commentRepository.save(comment);
        log.debug("Comment id={} added to item id={}", saved.getId(), itemId);
        return CommentMapper.toDto(saved);
    }

    private void fillBookingsForOwner(ItemDto dto, Long itemId) {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> last = bookingRepository
                .findAllByItemIdAndStatusAndStartBeforeOrderByStartDesc(
                        itemId, BookingStatus.APPROVED, now);
        List<Booking> next = bookingRepository
                .findAllByItemIdAndStatusAndStartAfterOrderByStartAsc(
                        itemId, BookingStatus.APPROVED, now);

        if (!last.isEmpty()) {
            dto.setLastBooking(BookingMapper.toBookingShortDto(last.get(0)));
        }
        if (!next.isEmpty()) {
            dto.setNextBooking(BookingMapper.toBookingShortDto(next.get(0)));
        }
    }

    private List<CommentDto> loadComments(Long itemId) {
        return commentRepository.findAllByItemIdOrderByCreatedDesc(itemId).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + itemId));
    }
}