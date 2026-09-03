package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemService itemService;

    @Test
    void create_shouldReturnItem() throws Exception {
        ItemDto item = makeItemDto(1L, "Drill", "Powerful drill");
        when(itemService.create(eq(1L), any())).thenReturn(item);

        mvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(makeItemDto(null, "Drill", "Powerful drill"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drill"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void update_shouldReturnUpdatedItem() throws Exception {
        ItemDto updated = makeItemDto(1L, "Drill v2", "Powerful drill");
        when(itemService.update(eq(1L), eq(1L), any())).thenReturn(updated);

        mvc.perform(patch("/items/1")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(makeItemDto(null, "Drill v2", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Drill v2"));
    }

    @Test
    void getById_shouldReturnItem() throws Exception {
        when(itemService.getById(1L, 1L)).thenReturn(makeItemDto(1L, "Drill", "Powerful drill"));

        mvc.perform(get("/items/1").header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void getAllByOwner_shouldReturnList() throws Exception {
        when(itemService.getAllByOwner(1L)).thenReturn(List.of(makeItemDto(1L, "Drill", "Powerful drill")));

        mvc.perform(get("/items").header(USER_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void search_shouldReturnFoundItems() throws Exception {
        when(itemService.search("drill")).thenReturn(List.of(makeItemDto(1L, "Drill", "Powerful drill")));

        mvc.perform(get("/items/search").param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void addComment_shouldReturnComment() throws Exception {
        CommentDto comment = new CommentDto();
        comment.setId(1L);
        comment.setText("Great item");
        comment.setAuthorName("John");
        comment.setCreated(LocalDateTime.of(2025, 1, 1, 12, 0));
        when(itemService.addComment(eq(1L), eq(1L), any())).thenReturn(comment);

        CommentRequestDto request = new CommentRequestDto();
        request.setText("Great item");

        mvc.perform(post("/items/1/comment")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Great item"))
                .andExpect(jsonPath("$.authorName").value("John"));
    }

    private ItemDto makeItemDto(Long id, String name, String description) {
        ItemDto dto = new ItemDto();
        dto.setId(id);
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(true);
        return dto;
    }
}