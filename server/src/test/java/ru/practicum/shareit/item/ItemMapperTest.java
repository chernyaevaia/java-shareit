package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;


import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    @Test
    void toItem_shouldMapAllFields() {
        ItemDto dto = new ItemDto();
        dto.setId(1L);
        dto.setName("Drill");
        dto.setDescription("Powerful drill");
        dto.setAvailable(true);

        Item item = ItemMapper.toItem(dto);

        assertThat(item.getId()).isEqualTo(1L);
        assertThat(item.getName()).isEqualTo("Drill");
        assertThat(item.getDescription()).isEqualTo("Powerful drill");
        assertThat(item.getAvailable()).isTrue();
    }

    @Test
    void toItemDto_withOwnerAndRequest_shouldMapAllFields() {
        User owner = makeUser(1L);
        ItemRequest request = ItemRequest.builder().id(5L).build();
        Item item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("Powerful drill");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);

        ItemDto dto = ItemMapper.toItemDto(item);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getOwnerId()).isEqualTo(1L);
        assertThat(dto.getRequestId()).isEqualTo(5L);
    }

    @Test
    void toItemDto_withoutOwnerAndRequest_shouldMapNullFields() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Drill");
        item.setDescription("Powerful drill");
        item.setAvailable(true);

        ItemDto dto = ItemMapper.toItemDto(item);

        assertThat(dto.getOwnerId()).isNull();
        assertThat(dto.getRequestId()).isNull();
    }

    private User makeUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setName("user" + id);
        u.setEmail("user" + id + "@mail.com");
        return u;
    }
}