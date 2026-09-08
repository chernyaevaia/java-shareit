package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperTest {

    @Test
    void toBooking_shouldMapAllFields() {
        User booker = makeUser(1L);
        Item item = makeItem(1L, makeUser(2L));
        BookingRequestDto dto = new BookingRequestDto();
        dto.setStart(LocalDateTime.of(2025, 1, 1, 12, 0));
        dto.setEnd(LocalDateTime.of(2025, 1, 2, 12, 0));

        Booking booking = BookingMapper.toBooking(dto, item, booker);

        assertThat(booking.getStart()).isEqualTo(dto.getStart());
        assertThat(booking.getEnd()).isEqualTo(dto.getEnd());
        assertThat(booking.getItem()).isEqualTo(item);
        assertThat(booking.getBooker()).isEqualTo(booker);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void toBookingDto_shouldMapAllFields() {
        User booker = makeUser(1L);
        Item item = makeItem(1L, makeUser(2L));
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.of(2025, 1, 1, 12, 0));
        booking.setEnd(LocalDateTime.of(2025, 1, 2, 12, 0));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);

        BookingDto dto = BookingMapper.toBookingDto(booking);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(booking.getStart());
        assertThat(dto.getEnd()).isEqualTo(booking.getEnd());
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(dto.getItem().getId()).isEqualTo(1L);
        assertThat(dto.getBooker().getId()).isEqualTo(1L);
    }

    @Test
    void toBookingShortDto_shouldMapAllFields() {
        User booker = makeUser(1L);
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.of(2025, 1, 1, 12, 0));
        booking.setEnd(LocalDateTime.of(2025, 1, 2, 12, 0));

        BookingShortDto dto = BookingMapper.toBookingShortDto(booking);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getBookerId()).isEqualTo(1L);
    }

    private User makeUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setName("user" + id);
        u.setEmail("user" + id + "@mail.com");
        return u;
    }

    private Item makeItem(Long id, User owner) {
        Item item = new Item();
        item.setId(id);
        item.setName("Drill");
        item.setAvailable(true);
        item.setOwner(owner);
        return item;
    }
}