package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void create_shouldReturnBooking() throws Exception {
        when(bookingService.create(eq(1L), any())).thenReturn(makeBookingDto());

        BookingRequestDto request = new BookingRequestDto();
        request.setItemId(1L);
        request.setStart(LocalDateTime.now().plusDays(1));
        request.setEnd(LocalDateTime.now().plusDays(2));

        mvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void approve_shouldReturnApprovedBooking() throws Exception {
        BookingDto dto = makeBookingDto();
        dto.setStatus(BookingStatus.APPROVED);
        when(bookingService.approve(1L, 1L, true)).thenReturn(dto);

        mvc.perform(patch("/bookings/1")
                        .header(USER_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getById_shouldReturnBooking() throws Exception {
        when(bookingService.getById(1L, 1L)).thenReturn(makeBookingDto());

        mvc.perform(get("/bookings/1").header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAllByBooker_shouldReturnList() throws Exception {
        when(bookingService.getAllByBooker(1L, BookingState.ALL)).thenReturn(List.of(makeBookingDto()));

        mvc.perform(get("/bookings")
                        .header(USER_HEADER, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAllByOwner_shouldReturnList() throws Exception {
        when(bookingService.getAllByOwner(1L, BookingState.ALL)).thenReturn(List.of(makeBookingDto()));

        mvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    private BookingDto makeBookingDto() {
        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setStart(LocalDateTime.of(2025, 1, 1, 12, 0));
        dto.setEnd(LocalDateTime.of(2025, 1, 2, 12, 0));
        dto.setStatus(BookingStatus.WAITING);
        return dto;
    }
}