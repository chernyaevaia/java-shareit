package ru.practicum.shareit.user;

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
import ru.practicum.shareit.user.dto.UserDto;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserClientTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private UserClient userClient;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.uriTemplateHandler(any())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.requestFactory(any(Supplier.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        userClient = new UserClient("http://localhost:9090", restTemplateBuilder);
    }

    @Test
    void create_shouldCallPostWithCorrectPath() {
        UserDto dto = new UserDto(1L, "John", "john@example.com");
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok(dto);

        when(restTemplate.exchange(eq(""), eq(HttpMethod.POST), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = userClient.create(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void update_shouldCallPatchWithCorrectPath() {
        UserDto dto = new UserDto(1L, "John Updated", "john@example.com");
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok(dto);

        when(restTemplate.exchange(eq("/1"), eq(HttpMethod.PATCH), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = userClient.update(1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void getById_shouldCallGetWithCorrectPath() {
        UserDto dto = new UserDto(1L, "John", "john@example.com");
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok(dto);

        when(restTemplate.exchange(eq("/1"), eq(HttpMethod.GET), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = userClient.getById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void getAll_shouldCallGetWithEmptyPath() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("users");

        when(restTemplate.exchange(eq(""), eq(HttpMethod.GET), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = userClient.getAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void delete_shouldCallDeleteWithCorrectPath() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.noContent().build();

        when(restTemplate.exchange(eq("/1"), eq(HttpMethod.DELETE), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = userClient.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}