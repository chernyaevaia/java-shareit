package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    @Test
    void getById_notFound_shouldThrow() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(makeUser(1L)));
        when(itemRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.getById(1L, 99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getOwnRequests_emptyList_shouldReturnEmpty() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(makeUser(1L)));
        when(itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(1L)).thenReturn(List.of());

        assertThat(requestService.getOwnRequests(1L)).isEmpty();
    }

    @Test
    void getAllRequests_shouldExcludeOwn() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(makeUser(1L)));
        ItemRequest other = ItemRequest.builder()
                .id(2L)
                .description("need tent")
                .requestor(makeUser(2L))
                .created(LocalDateTime.now())
                .build();
        when(itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(any(Long.class), any(Pageable.class)))
                .thenReturn(List.of(other));
        when(itemRepository.findAllByRequestIdIn(List.of(2L))).thenReturn(List.of());

        List<ItemRequestResponseDto> result = requestService.getAllRequests(1L, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(2L);
    }

    private User makeUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setName("user" + id);
        u.setEmail("user" + id + "@mail.com");
        return u;
    }
}