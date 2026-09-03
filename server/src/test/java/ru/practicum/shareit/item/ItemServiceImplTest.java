package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void create_withRequestId_shouldLinkRequest() {
        User owner = makeUser(1L);
        ItemRequest request = ItemRequest.builder().id(5L).description("need").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(5L)).thenReturn(Optional.of(request));
        when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ItemDto dto = makeItemDto(null, "Drill", "desc");
        dto.setRequestId(5L);

        ItemDto result = itemService.create(1L, dto);

        assertThat(result.getRequestId()).isEqualTo(5L);
    }

    @Test
    void create_withWrongRequestId_shouldThrowNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(makeUser(1L)));
        when(itemRequestRepository.findById(99L)).thenReturn(Optional.empty());

        ItemDto dto = makeItemDto(null, "Drill", "desc");
        dto.setRequestId(99L);

        assertThatThrownBy(() -> itemService.create(1L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_withWrongUser_shouldThrowNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.create(99L, makeItemDto(null, "a", "b")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_byNonOwner_shouldThrowForbidden() {
        User owner = makeUser(1L);
        Item item = makeItem(1L, owner);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.update(2L, 1L, makeItemDto(null, "new", null)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void update_withBlankName_shouldKeepOldName() {
        User owner = makeUser(1L);
        Item item = makeItem(1L, owner);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ItemDto patch = new ItemDto();
        patch.setName("  ");
        patch.setAvailable(false);

        ItemDto result = itemService.update(1L, 1L, patch);

        assertThat(result.getName()).isEqualTo("Drill");
        assertThat(result.getAvailable()).isFalse();
    }

    @Test
    void update_wrongItem_shouldThrowNotFound() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.update(1L, 99L, new ItemDto()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_byNonOwner_shouldNotIncludeBookings() {
        User owner = makeUser(1L);
        Item item = makeItem(1L, owner);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemIdOrderByCreatedDesc(1L)).thenReturn(List.of());

        ItemDto result = itemService.getById(1L, 2L);

        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
        verify(bookingRepository, never())
                .findAllByItemIdAndStatusAndStartBeforeOrderByStartDesc(anyLong(), any(), any());
    }

    @Test
    void getAllByOwner_noItems_shouldReturnEmpty() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(makeUser(1L)));
        when(itemRepository.findAllByOwnerIdOrderByIdAsc(1L)).thenReturn(List.of());

        assertThat(itemService.getAllByOwner(1L)).isEmpty();
    }

    @Test
    void search_blankText_shouldReturnEmpty() {
        assertThat(itemService.search("")).isEmpty();
        assertThat(itemService.search(null)).isEmpty();
        verifyNoInteractions(itemRepository);
    }

    @Test
    void addComment_withoutPastBooking_shouldThrowValidation() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(makeUser(2L)));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(makeItem(1L, makeUser(1L))));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(anyLong(), anyLong(), any(), any()))
                .thenReturn(false);

        CommentRequestDto comment = new CommentRequestDto();
        comment.setText("nice");

        assertThatThrownBy(() -> itemService.addComment(2L, 1L, comment))
                .isInstanceOf(ValidationException.class);
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
        item.setDescription("desc");
        item.setAvailable(true);
        item.setOwner(owner);
        return item;
    }

    private ItemDto makeItemDto(Long id, String name, String description) {
        ItemDto dto = new ItemDto();
        dto.setId(id);
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(true);
        return dto;
    }
}