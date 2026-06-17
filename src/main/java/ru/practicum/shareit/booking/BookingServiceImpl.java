package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public BookingDto create(Long userId, BookingRequestDto dto) {
        log.info("Creating booking for user id={} on item id={}", userId, dto.getItemId());

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + dto.getItemId()));

        if (!item.getAvailable()) {
            throw new ValidationException("Item is not available for booking");
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Owner cannot book his own item");
        }

        if (dto.getEnd().isBefore(dto.getStart()) || dto.getEnd().equals(dto.getStart())) {
            throw new ValidationException("End date must be after start date");
        }

        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking saved = bookingRepository.save(booking);
        log.debug("Booking id={} created with status WAITING", saved.getId());
        return BookingMapper.toBookingDto(saved);
    }

    @Override
    public BookingDto approve(Long ownerId, Long bookingId, Boolean approved) {
        log.info("Approving booking id={} by owner id={}, approved={}", bookingId, ownerId, approved);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Only owner can approve or reject booking");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Booking status is already " + booking.getStatus());
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking saved = bookingRepository.save(booking);
        return BookingMapper.toBookingDto(saved);
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        log.info("Fetching booking id={} for user id={}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new ForbiddenException("Only booker or owner can view booking");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllByBooker(Long userId, BookingState state) {
        log.info("Fetching bookings for booker id={} with state={}", userId, state);
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> result = new ArrayList<>();

        switch (state) {
            case ALL -> result = bookingRepository.findAllByBookerIdOrderByStartDesc(userId);
            case CURRENT -> result = bookingRepository
                    .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now);
            case PAST -> result = bookingRepository
                    .findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> result = bookingRepository
                    .findAllByBookerIdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING -> result = bookingRepository
                    .findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED -> result = bookingRepository
                    .findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
        }

        return result.stream().map(BookingMapper::toBookingDto).toList();
    }

    @Override
    public List<BookingDto> getAllByOwner(Long ownerId, BookingState state) {
        log.info("Fetching bookings for owner id={} with state={}", ownerId, state);

        if (itemRepository.findAllByOwnerIdOrderByIdAsc(ownerId).isEmpty()) {
            throw new NotFoundException("User id=" + ownerId + " does not own any items");
        }

        LocalDateTime now = LocalDateTime.now();
        List<Booking> result = new ArrayList<>();

        switch (state) {
            case ALL -> result = bookingRepository.findAllByItemOwnerIdOrderByStartDesc(ownerId);
            case CURRENT -> result = bookingRepository
                    .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(ownerId, now, now);
            case PAST -> result = bookingRepository
                    .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(ownerId, now);
            case FUTURE -> result = bookingRepository
                    .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(ownerId, now);
            case WAITING -> result = bookingRepository
                    .findAllByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING);
            case REJECTED -> result = bookingRepository
                    .findAllByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED);
        }

        return result.stream().map(BookingMapper::toBookingDto).toList();
    }
}