package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void create_valid_shouldReturn200() throws Exception {
        when(itemClient.create(anyLong(), any())).thenReturn(ResponseEntity.ok().build());

        mvc.perform(post("/items")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(makeValidItem())))
                .andExpect(status().isOk());
    }

    @Test
    void create_blankName_shouldReturn400() throws Exception {
        ItemDto dto = makeValidItem();
        dto.setName("");

        mvc.perform(post("/items")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_nullAvailable_shouldReturn400() throws Exception {
        ItemDto dto = makeValidItem();
        dto.setAvailable(null);

        mvc.perform(post("/items")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        when(itemClient.update(anyLong(), anyLong(), any())).thenReturn(ResponseEntity.ok().build());

        mvc.perform(patch("/items/1")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(itemClient.getById(anyLong(), anyLong())).thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/items/1").header(HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllByOwner_shouldReturn200() throws Exception {
        when(itemClient.getAllByOwner(1L)).thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/items").header(HEADER, 1L))
                .andExpect(status().isOk());
    }

    @Test
    void search_shouldReturn200() throws Exception {
        when(itemClient.search("drill")).thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/items/search").param("text", "drill"))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_valid_shouldReturn200() throws Exception {
        when(itemClient.addComment(anyLong(), anyLong(), any())).thenReturn(ResponseEntity.ok().build());

        CommentRequestDto comment = new CommentRequestDto();
        comment.setText("Nice");

        mvc.perform(post("/items/1/comment")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(comment)))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_blankText_shouldReturn400() throws Exception {
        CommentRequestDto comment = new CommentRequestDto();
        comment.setText("");

        mvc.perform(post("/items/1/comment")
                        .header(HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(comment)))
                .andExpect(status().isBadRequest());
    }

    private ItemDto makeValidItem() {
        ItemDto dto = new ItemDto();
        dto.setName("Drill");
        dto.setDescription("Powerful drill");
        dto.setAvailable(true);
        return dto;
    }
}