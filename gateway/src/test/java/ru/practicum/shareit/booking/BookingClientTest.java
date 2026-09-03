package ru.practicum.shareit.booking;

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
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingClientTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    private BookingClient bookingClient;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.uriTemplateHandler(any())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.requestFactory(any(Supplier.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        bookingClient = new BookingClient("http://localhost:9090", restTemplateBuilder);
    }

    @Test
    void create_shouldCallPostWithCorrectPath() {
        BookingRequestDto dto = new BookingRequestDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok(dto);

        when(restTemplate.exchange(eq(""), eq(HttpMethod.POST), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = bookingClient.create(1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dto);
    }

    @Test
    void approve_shouldCallPatchWithCorrectPath() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("approved");

        when(restTemplate.exchange(eq("/1?approved={approved}"), eq(HttpMethod.PATCH), any(), eq(Object.class), any(Map.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = bookingClient.approve(1L, 1L, true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getById_shouldCallGetWithCorrectPath() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("booking");

        when(restTemplate.exchange(eq("/1"), eq(HttpMethod.GET), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = bookingClient.getById(1L, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAllByBooker_shouldCallGetWithCorrectPath() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("bookings");

        when(restTemplate.exchange(eq("?state={state}&from={from}&size={size}"), eq(HttpMethod.GET), any(), eq(Object.class), any(Map.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = bookingClient.getAllByBooker(1L, "ALL", 0, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAllByOwner_shouldCallGetWithCorrectPath() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("owner bookings");

        when(restTemplate.exchange(eq("/owner?state={state}&from={from}&size={size}"), eq(HttpMethod.GET), any(), eq(Object.class), any(Map.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = bookingClient.getAllByOwner(1L, "ALL", 0, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}