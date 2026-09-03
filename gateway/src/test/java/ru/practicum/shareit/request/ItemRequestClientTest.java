package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestClientTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private ItemRequestClient itemRequestClient;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.uriTemplateHandler(any())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.requestFactory(any(Supplier.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        itemRequestClient = new ItemRequestClient("http://localhost:9090", restTemplateBuilder);
    }

    @Test
    void create_shouldCallPostWithCorrectPath() {
        ItemRequestDto dto = new ItemRequestDto("Need a drill");
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok(dto);

        when(restTemplate.exchange(eq(""), eq(HttpMethod.POST), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemRequestClient.create(1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void getOwnRequests_shouldCallGetWithEmptyPath() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("requests");

        when(restTemplate.exchange(eq(""), eq(HttpMethod.GET), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemRequestClient.getOwnRequests(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAllRequests_shouldCallGetWithPaginationPath() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("all requests");

        when(restTemplate.exchange(eq("/all?from={from}&size={size}"), eq(HttpMethod.GET), any(), eq(Object.class), any(Map.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemRequestClient.getAllRequests(1L, 0, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getById_shouldCallGetWithCorrectPath() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("request");

        when(restTemplate.exchange(eq("/1"), eq(HttpMethod.GET), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemRequestClient.getById(1L, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}