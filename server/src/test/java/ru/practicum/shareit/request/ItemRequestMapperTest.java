package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestMapperTest {

    @Test
    void toItemRequest_shouldMapFields() {
        User requestor = makeUser(1L);
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a tent");

        ItemRequest request = ItemRequestMapper.toItemRequest(dto, requestor);

        assertThat(request.getDescription()).isEqualTo("Need a tent");
        assertThat(request.getRequestor()).isEqualTo(requestor);
        assertThat(request.getCreated()).isNotNull();
    }

    @Test
    void toResponseDto_withItems_shouldMapAll() {
        User requestor = makeUser(1L);
        User owner = makeUser(2L);
        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Need a tent")
                .requestor(requestor)
                .created(LocalDateTime.of(2025, 1, 1, 12, 0))
                .build();
        Item item = new Item();
        item.setId(5L);
        item.setName("Tent");
        item.setOwner(owner);

        ItemRequestResponseDto dto = ItemRequestMapper.toResponseDto(request, List.of(item));

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Need a tent");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2025, 1, 1, 12, 0));
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getId()).isEqualTo(5L);
        assertThat(dto.getItems().get(0).getOwnerId()).isEqualTo(2L);
    }

    @Test
    void toResponseDto_withNullItems_shouldReturnEmptyList() {
        User requestor = makeUser(1L);
        ItemRequest request = ItemRequest.builder()
                .id(1L)
                .description("Need a tent")
                .requestor(requestor)
                .created(LocalDateTime.of(2025, 1, 1, 12, 0))
                .build();

        ItemRequestResponseDto dto = ItemRequestMapper.toResponseDto(request, null);

        assertThat(dto.getItems()).isEmpty();
    }

    @Test
    void toItemResponseDto_shouldMapFields() {
        User owner = makeUser(2L);
        Item item = new Item();
        item.setId(5L);
        item.setName("Tent");
        item.setOwner(owner);

        ItemResponseDto dto = ItemRequestMapper.toItemResponseDto(item);

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getName()).isEqualTo("Tent");
        assertThat(dto.getOwnerId()).isEqualTo(2L);
    }

    private User makeUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setName("user" + id);
        u.setEmail("user" + id + "@mail.com");
        return u;
    }
}