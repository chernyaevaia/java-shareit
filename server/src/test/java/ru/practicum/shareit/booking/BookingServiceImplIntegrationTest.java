package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplIntegrationTest {

    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Test
    void create_shouldCreateWaitingBooking() {
        User owner = saveUser("owner", "owner@example.com");
        User booker = saveUser("booker", "booker@example.com");
        Item item = saveItem(owner);

        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(item.getId());
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));

        BookingDto result = bookingService.create(booker.getId(), dto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(result.getItem().getId()).isEqualTo(item.getId());
        assertThat(result.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void getAllByBooker_shouldReturnBookings() {
        User owner = saveUser("owner2", "owner2@example.com");
        User booker = saveUser("booker2", "booker2@example.com");
        Item item = saveItem(owner);

        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(item.getId());
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));
        bookingService.create(booker.getId(), dto);

        List<BookingDto> bookings = bookingService.getAllByBooker(booker.getId(), BookingState.ALL);

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getBooker().getId()).isEqualTo(booker.getId());
    }

    private User saveUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    private Item saveItem(User owner) {
        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Powerful drill");
        item.setAvailable(true);
        item.setOwner(owner);
        return itemRepository.save(item);
    }
}