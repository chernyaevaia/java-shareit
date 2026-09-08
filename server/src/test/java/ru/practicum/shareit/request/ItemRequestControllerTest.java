package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestService requestService;

    @Test
    void create_shouldReturnRequest() throws Exception {
        when(requestService.create(eq(1L), any())).thenReturn(makeResponseDto());

        ItemRequestDto request = new ItemRequestDto();
        request.setDescription("Need a tent");

        mvc.perform(post("/requests")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a tent"));
    }

    @Test
    void getOwnRequests_shouldReturnList() throws Exception {
        when(requestService.getOwnRequests(1L)).thenReturn(List.of(makeResponseDto()));

        mvc.perform(get("/requests").header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Need a tent"));
    }

    @Test
    void getAllRequests_shouldReturnList() throws Exception {
        when(requestService.getAllRequests(1L, 0, 10)).thenReturn(List.of(makeResponseDto()));

        mvc.perform(get("/requests/all")
                        .header(USER_HEADER, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getById_shouldReturnRequest() throws Exception {
        when(requestService.getById(1L, 1L)).thenReturn(makeResponseDto());

        mvc.perform(get("/requests/1").header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Need a tent"));
    }

    private ItemRequestResponseDto makeResponseDto() {
        return ItemRequestResponseDto.builder()
                .id(1L)
                .description("Need a tent")
                .created(LocalDateTime.of(2025, 1, 1, 12, 0))
                .items(List.of())
                .build();
    }
}