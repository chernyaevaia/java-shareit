package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplIntegrationTest {

    private final ItemService itemService;
    private final UserRepository userRepository;

    private User owner;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setName("owner");
        user.setEmail("owner@example.com");
        owner = userRepository.save(user);
    }

    @Test
    void getAllByOwner_shouldReturnOwnerItems() {
        itemService.create(owner.getId(), makeItemDto("Drill", "Powerful drill"));
        itemService.create(owner.getId(), makeItemDto("Bike", "Mountain bike"));

        List<ItemDto> items = itemService.getAllByOwner(owner.getId());

        assertThat(items).hasSize(2);
        assertThat(items.get(0).getName()).isEqualTo("Drill");
        assertThat(items.get(1).getName()).isEqualTo("Bike");
        assertThat(items.get(0).getOwnerId()).isEqualTo(owner.getId());
    }

    @Test
    void search_shouldFindAvailableItemsByText() {
        itemService.create(owner.getId(), makeItemDto("Drill", "Powerful drill"));
        itemService.create(owner.getId(), makeItemDto("Bike", "Mountain bike"));

        List<ItemDto> found = itemService.search("drill");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Drill");
    }

    private ItemDto makeItemDto(String name, String description) {
        ItemDto dto = new ItemDto();
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(true);
        return dto;
    }
}