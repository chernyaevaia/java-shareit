package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Test
    void create_valid_shouldReturn200() throws Exception {
        when(itemRequestClient.create(anyLong(), any())).thenReturn(ResponseEntity.ok().build());

        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Need a tent");

        mvc.perform(post("/requests")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void create_blankDescription_shouldReturn400() throws Exception {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("");

        mvc.perform(post("/requests")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    void getOwnRequests_shouldReturn200() throws Exception {
        when(itemRequestClient.getOwnRequests(1L)).thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/requests").header(HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRequests_validParams_shouldReturn200() throws Exception {
        when(itemRequestClient.getAllRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/requests/all")
                        .header(HEADER, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRequests_negativeFrom_shouldReturn400() throws Exception {
        mvc.perform(get("/requests/all")
                        .header(HEADER, 1L)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    void getAllRequests_zeroSize_shouldReturn400() throws Exception {
        mvc.perform(get("/requests/all")
                        .header(HEADER, 1L)
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(itemRequestClient);
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(itemRequestClient.getById(anyLong(), anyLong())).thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/requests/1").header(HEADER, 1L))
                .andExpect(status().isOk());
    }
}