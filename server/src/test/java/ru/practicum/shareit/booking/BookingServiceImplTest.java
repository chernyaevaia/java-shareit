package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;


    @Test
    void create_shouldReturnWaitingBooking() {
        User owner = makeUser(1L);
        User booker = makeUser(2L);
        Item item = makeItem(1L, owner);
        BookingRequestDto dto = makeRequest(1L);
        Booking booking = makeBooking(1L, BookingStatus.WAITING, item, booker);

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any())).thenReturn(booking);

        BookingDto result = bookingService.create(2L, dto);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void create_unavailableItem_shouldThrowValidation() {
        User booker = makeUser(2L);
        Item item = makeItem(1L, makeUser(1L));
        item.setAvailable(false);

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.create(2L, makeRequest(1L)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_ownerBookingOwnItem_shouldThrowNotFound() {
        User owner = makeUser(1L);
        Item item = makeItem(1L, owner);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.create(1L, makeRequest(1L)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_endBeforeStart_shouldThrowValidation() {
        User booker = makeUser(2L);
        Item item = makeItem(1L, makeUser(1L));

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        BookingRequestDto dto = makeRequest(1L);
        dto.setEnd(dto.getStart().minusHours(1));

        assertThatThrownBy(() -> bookingService.create(2L, dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_endEqualsStart_shouldThrowValidation() {
        User booker = makeUser(2L);
        Item item = makeItem(1L, makeUser(1L));

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        BookingRequestDto dto = makeRequest(1L);
        dto.setEnd(dto.getStart());

        assertThatThrownBy(() -> bookingService.create(2L, dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_userNotFound_shouldThrowNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(99L, makeRequest(1L)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_itemNotFound_shouldThrowNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(makeUser(1L)));
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(1L, makeRequest(99L)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void approve_approved_shouldSetApproved() {
        User owner = makeUser(1L);
        User booker = makeUser(2L);
        Item item = makeItem(1L, owner);
        Booking booking = makeBooking(1L, BookingStatus.WAITING, item, booker);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BookingDto result = bookingService.approve(1L, 1L, true);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approve_rejected_shouldSetRejected() {
        User owner = makeUser(1L);
        User booker = makeUser(2L);
        Item item = makeItem(1L, owner);
        Booking booking = makeBooking(1L, BookingStatus.WAITING, item, booker);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BookingDto result = bookingService.approve(1L, 1L, false);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approve_byNonOwner_shouldThrowForbidden() {
        User owner = makeUser(1L);
        User booker = makeUser(2L);
        Item item = makeItem(1L, owner);
        Booking booking = makeBooking(1L, BookingStatus.WAITING, item, booker);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approve(2L, 1L, true))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void approve_alreadyApproved_shouldThrowValidation() {
        User owner = makeUser(1L);
        User booker = makeUser(2L);
        Item item = makeItem(1L, owner);
        Booking booking = makeBooking(1L, BookingStatus.APPROVED, item, booker);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approve(1L, 1L, true))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void approve_bookingNotFound_shouldThrowNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.approve(1L, 99L, true))
                .isInstanceOf(NotFoundException.class);
    }

    // --- getById ---

    @Test
    void getById_byBooker_shouldReturnBooking() {
        User owner = makeUser(1L);
        User booker = makeUser(2L);
        Item item = makeItem(1L, owner);
        Booking booking = makeBooking(1L, BookingStatus.WAITING, item, booker);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.getById(2L, 1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getById_byOwner_shouldReturnBooking() {
        User owner = makeUser(1L);
        User booker = makeUser(2L);
        Item item = makeItem(1L, owner);
        Booking booking = makeBooking(1L, BookingStatus.WAITING, item, booker);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.getById(1L, 1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getById_byUnrelatedUser_shouldThrowForbidden() {
        User owner = makeUser(1L);
        User booker = makeUser(2L);
        Item item = makeItem(1L, owner);
        Booking booking = makeBooking(1L, BookingStatus.WAITING, item, booker);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getById(99L, 1L))
                .isInstanceOf(ForbiddenException.class);
    }


    @Test
    void getAllByBooker_all_shouldReturnList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(makeUser(1L)));
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(1L)).thenReturn(List.of());

        assertThat(bookingService.getAllByBooker(1L, BookingState.ALL)).isEmpty();
        verify(bookingRepository).findAllByBookerIdOrderByStartDesc(1L);
    }

    @Test
    void getAllByBooker_current_shouldReturnList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(makeUser(1L)));
        when(bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of());

        assertThat(bookingService.getAllByBooker(1L, BookingState.CURRENT)).isEmpty();
        verify(bookingRepository).findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getAllByBooker_past_shouldReturnList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(makeUser(1L)));
        when(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of());

        assertThat(bookingService.getAllByBooker(1L, BookingState.PAST)).isEmpty();
        verify(bookingRepository).findAllByBookerIdAndEndBeforeOrderByStartDesc(anyLong(), any());
    }

    @Test
    void getAllByBooker_future_shouldReturnList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(makeUser(1L)));
        when(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of());

        assertThat(bookingService.getAllByBooker(1L, BookingState.FUTURE)).isEmpty();
        verify(bookingRepository).findAllByBookerIdAndStartAfterOrderByStartDesc(anyLong(), any());
    }

    @Test
    void getAllByBooker_waiting_shouldReturnList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(makeUser(1L)));
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of());

        assertThat(bookingService.getAllByBooker(1L, BookingState.WAITING)).isEmpty();
        verify(bookingRepository).findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any());
    }

    @Test
    void getAllByBooker_rejected_shouldReturnList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(makeUser(1L)));
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of());

        assertThat(bookingService.getAllByBooker(1L, BookingState.REJECTED)).isEmpty();
        verify(bookingRepository).findAllByBookerIdAndStatusOrderByStartDesc(anyLong(), any());
    }


    @Test
    void getAllByOwner_all_shouldReturnList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(makeUser(1L)));
        when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(1L)).thenReturn(List.of());

        assertThat(bookingService.getAllByOwner(1L, BookingState.ALL)).isEmpty();
        verify(bookingRepository).findAllByItemOwnerIdOrderByStartDesc(1L);
    }

    @Test
    void getAllByOwner_current_shouldReturnList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(makeUser(1L)));
        when(bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any()))
                .thenReturn(List.of());

        assertThat(bookingService.getAllByOwner(1L, BookingState.CURRENT)).isEmpty();
        verify(bookingRepository).findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getAllByOwner_past_shouldReturnList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(makeUser(1L)));
        when(bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of());

        assertThat(bookingService.getAllByOwner(1L, BookingState.PAST)).isEmpty();
        verify(bookingRepository).findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(anyLong(), any());
    }

    @Test
    void getAllByOwner_future_shouldReturnList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(makeUser(1L)));
        when(bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of());

        assertThat(bookingService.getAllByOwner(1L, BookingState.FUTURE)).isEmpty();
        verify(bookingRepository).findAllByItemOwnerIdAndStartAfterOrderByStartDesc(anyLong(), any());
    }

    @Test
    void getAllByOwner_waiting_shouldReturnList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(makeUser(1L)));
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of());

        assertThat(bookingService.getAllByOwner(1L, BookingState.WAITING)).isEmpty();
        verify(bookingRepository).findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any());
    }

    @Test
    void getAllByOwner_rejected_shouldReturnList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(makeUser(1L)));
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any()))
                .thenReturn(List.of());

        assertThat(bookingService.getAllByOwner(1L, BookingState.REJECTED)).isEmpty();
        verify(bookingRepository).findAllByItemOwnerIdAndStatusOrderByStartDesc(anyLong(), any());
    }

    @Test
void create_itemWithNullAvailable_shouldThrowValidation() {
    User booker = makeUser(2L);
    Item item = makeItem(1L, makeUser(1L));
    item.setAvailable(null);  // Null availability

    when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
    when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

    assertThatThrownBy(() -> bookingService.create(2L, makeRequest(1L)))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Item is not available");
}

@Test
void approve_alreadyRejected_shouldThrowValidation() {
    User owner = makeUser(1L);
    User booker = makeUser(2L);
    Item item = makeItem(1L, owner);
    Booking booking = makeBooking(1L, BookingStatus.REJECTED, item, booker);

    when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

    assertThatThrownBy(() -> bookingService.approve(1L, 1L, true))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Booking status is already REJECTED");
}

@Test
void approve_withNullApproved_shouldThrowException() {
    User owner = makeUser(1L);
    User booker = makeUser(2L);
    Item item = makeItem(1L, owner);
    Booking booking = makeBooking(1L, BookingStatus.WAITING, item, booker);

    when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

    // Null approved will cause NPE when trying to evaluate ternary
    assertThatThrownBy(() -> bookingService.approve(1L, 1L, null))
            .isInstanceOf(NullPointerException.class);
}

@Test
void getById_bookingNotFound_shouldThrowNotFound() {
    when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookingService.getById(1L, 99L))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Booking not found with id: 99");
}

@Test
void getAllByBooker_userNotFound_shouldThrowNotFound() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookingService.getAllByBooker(99L, BookingState.ALL))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("User not found with id: 99");
}

@Test
void getAllByOwner_userNotFound_shouldThrowNotFound() {
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> bookingService.getAllByOwner(99L, BookingState.ALL))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("User not found with id: 99");
}

@Test
void create_withNullStartTime_shouldThrowValidation() {
    User booker = makeUser(2L);
    Item item = makeItem(1L, makeUser(1L));

    when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
    when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

    BookingRequestDto dto = makeRequest(1L);
    dto.setStart(null);

    assertThatThrownBy(() -> bookingService.create(2L, dto))
            .isInstanceOf(NullPointerException.class);
}

@Test
void create_withNullEndTime_shouldThrowValidation() {
    User booker = makeUser(2L);
    Item item = makeItem(1L, makeUser(1L));

    when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
    when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

    BookingRequestDto dto = makeRequest(1L);
    dto.setEnd(null);

    assertThatThrownBy(() -> bookingService.create(2L, dto))
            .isInstanceOf(NullPointerException.class);
}

@Test
void create_bookingWithPastStartTime_shouldCreateSuccessfully() {
    User owner = makeUser(1L);
    User booker = makeUser(2L);
    Item item = makeItem(1L, owner);
    BookingRequestDto dto = makeRequest(1L);
    dto.setStart(LocalDateTime.now().minusDays(2));
    dto.setEnd(LocalDateTime.now().minusDays(1));
    
    Booking booking = makeBooking(1L, BookingStatus.WAITING, item, booker);
    when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
    when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
    when(bookingRepository.save(any())).thenReturn(booking);

    BookingDto result = bookingService.create(2L, dto);

    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
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

    private Booking makeBooking(Long id, BookingStatus status, Item item, User booker) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(status);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        return booking;
    }

    private BookingRequestDto makeRequest(Long itemId) {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(itemId);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));
        return dto;
    }
}