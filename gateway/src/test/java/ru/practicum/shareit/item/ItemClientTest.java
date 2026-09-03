package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemClientTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private ItemClient itemClient;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.uriTemplateHandler(any())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.requestFactory(any(Supplier.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        itemClient = new ItemClient("http://localhost:9090", restTemplateBuilder);
    }

    @Test
    void create_shouldCallPostWithCorrectPath() {
        ItemDto dto = new ItemDto(1L, "Item", "Description", true, null);
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok(dto);

        when(restTemplate.exchange(eq(""), eq(HttpMethod.POST), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemClient.create(1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void update_shouldCallPatchWithCorrectPath() {
        ItemDto dto = new ItemDto(1L, "Updated Item", "Updated Description", false, null);
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok(dto);

        when(restTemplate.exchange(eq("/1"), eq(HttpMethod.PATCH), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemClient.update(1L, 1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void getById_shouldCallGetWithCorrectPath() {
        ItemDto dto = new ItemDto(1L, "Item", "Description", true, null);
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok(dto);

        when(restTemplate.exchange(eq("/1"), eq(HttpMethod.GET), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemClient.getById(1L, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void getAllByOwner_shouldCallGetWithEmptyPath() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("items");

        when(restTemplate.exchange(eq(""), eq(HttpMethod.GET), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemClient.getAllByOwner(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void search_shouldCallGetWithSearchPath() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("search results");

        when(restTemplate.exchange(eq("/search?text={text}"), eq(HttpMethod.GET), any(), eq(Object.class), any(Map.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemClient.search("test");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void addComment_shouldCallPostWithCorrectPath() {
        CommentRequestDto dto = new CommentRequestDto("Great item!");
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok(dto);

        when(restTemplate.exchange(eq("/1/comment"), eq(HttpMethod.POST), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemClient.addComment(1L, 1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }
}