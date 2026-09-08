package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void create_valid_shouldReturn200() throws Exception {
        when(bookingClient.create(anyLong(), any())).thenReturn(ResponseEntity.ok().build());

        mvc.perform(post("/bookings")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(makeValidBooking())))
                .andExpect(status().isOk());
    }

    @Test
    void create_nullItemId_shouldReturn400() throws Exception {
        BookingRequestDto dto = makeValidBooking();
        dto.setItemId(null);

        mvc.perform(post("/bookings")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void create_pastStart_shouldReturn400() throws Exception {
        BookingRequestDto dto = makeValidBooking();
        dto.setStart(LocalDateTime.now().minusDays(1));

        mvc.perform(post("/bookings")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void create_pastEnd_shouldReturn400() throws Exception {
        BookingRequestDto dto = makeValidBooking();
        dto.setEnd(LocalDateTime.now().minusHours(1));

        mvc.perform(post("/bookings")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void create_endBeforeStart_shouldReturn400_withoutCallingClient() throws Exception {
        BookingRequestDto dto = makeValidBooking();
        dto.setStart(LocalDateTime.now().plusDays(2));
        dto.setEnd(LocalDateTime.now().plusDays(1));

        mvc.perform(post("/bookings")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void create_endEqualsStart_shouldReturn400() throws Exception {
        BookingRequestDto dto = makeValidBooking();
        dto.setEnd(dto.getStart());

        mvc.perform(post("/bookings")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void create_missingHeader_shouldReturn400() throws Exception {
        mvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(makeValidBooking())))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void approve_shouldReturn200() throws Exception {
        when(bookingClient.approve(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(patch("/bookings/1")
                        .header(HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(bookingClient.getById(anyLong(), anyLong())).thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/bookings/1").header(HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllByBooker_shouldReturn200_andPassParsedState() throws Exception {
        when(bookingClient.getAllByBooker(anyLong(), any(BookingState.class), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/bookings")
                        .header(HEADER, 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(bookingClient).getAllByBooker(1L, BookingState.ALL, 0, 10);
    }

    @Test
    void getAllByBooker_lowerCaseState_shouldBeAccepted() throws Exception {
        when(bookingClient.getAllByBooker(anyLong(), any(BookingState.class), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/bookings")
                        .header(HEADER, 1L)
                        .param("state", "current"))
                .andExpect(status().isOk());

        verify(bookingClient).getAllByBooker(1L, BookingState.CURRENT, 0, 10);
    }

    @Test
    void getAllByBooker_unknownState_shouldReturn400_withoutCallingClient() throws Exception {
        mvc.perform(get("/bookings")
                        .header(HEADER, 1L)
                        .param("state", "UNSUPPORTED")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void getAllByBooker_negativeFrom_shouldReturn400() throws Exception {
        mvc.perform(get("/bookings")
                        .header(HEADER, 1L)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void getAllByOwner_shouldReturn200() throws Exception {
        when(bookingClient.getAllByOwner(anyLong(), any(BookingState.class), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/bookings/owner")
                        .header(HEADER, 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllByOwner_unknownState_shouldReturn400() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .header(HEADER, 1L)
                        .param("state", "WRONG"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    @Test
    void getAllByOwner_zeroSize_shouldReturn400() throws Exception {
        mvc.perform(get("/bookings/owner")
                        .header(HEADER, 1L)
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingClient);
    }

    private BookingRequestDto makeValidBooking() {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.now().plusDays(1));
        dto.setEnd(LocalDateTime.now().plusDays(2));
        return dto;
    }
}