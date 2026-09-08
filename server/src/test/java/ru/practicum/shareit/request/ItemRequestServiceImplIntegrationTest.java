package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceImplIntegrationTest {

    private final ItemRequestService requestService;
    private final ItemService itemService;
    private final UserRepository userRepository;

    @Test
    void create_shouldPersistRequest() {
        User requestor = saveUser("requestor", "requestor@example.com");

        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a tent");

        ItemRequestResponseDto created = requestService.create(requestor.getId(), dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Need a tent");
        assertThat(created.getCreated()).isNotNull();
        assertThat(created.getItems()).isEmpty();
    }

    @Test
    void getOwnRequests_shouldReturnRequestsWithResponses() {
        User requestor = saveUser("requestor2", "requestor2@example.com");
        User owner = saveUser("owner", "owner@example.com");

        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a tent");
        ItemRequestResponseDto created = requestService.create(requestor.getId(), dto);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Tent");
        itemDto.setDescription("Two person tent");
        itemDto.setAvailable(true);
        itemDto.setRequestId(created.getId());
        itemService.create(owner.getId(), itemDto);

        List<ItemRequestResponseDto> own = requestService.getOwnRequests(requestor.getId());

        assertThat(own).hasSize(1);
        assertThat(own.get(0).getItems()).hasSize(1);
        assertThat(own.get(0).getItems().get(0).getName()).isEqualTo("Tent");
        assertThat(own.get(0).getItems().get(0).getOwnerId()).isEqualTo(owner.getId());
    }

    private User saveUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }
}