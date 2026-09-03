package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserClient userClient;

    @Test
    void create_valid_shouldReturn200() throws Exception {
        when(userClient.create(any())).thenReturn(ResponseEntity.ok().build());

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(makeValidUser())))
                .andExpect(status().isOk());
    }

    @Test
    void create_blankName_shouldReturn400() throws Exception {
        UserDto dto = makeValidUser();
        dto.setName("");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_invalidEmail_shouldReturn400() throws Exception {
        UserDto dto = makeValidUser();
        dto.setEmail("not-an-email");

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturn200() throws Exception {
        when(userClient.update(anyLong(), any())).thenReturn(ResponseEntity.ok().build());

        mvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(makeValidUser())))
                .andExpect(status().isOk());
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        when(userClient.getById(1L)).thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        when(userClient.getAll()).thenReturn(ResponseEntity.ok().build());

        mvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    void delete_shouldReturn200() throws Exception {
        when(userClient.delete(1L)).thenReturn(ResponseEntity.ok().build());

        mvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }

    private UserDto makeValidUser() {
        UserDto dto = new UserDto();
        dto.setName("John");
        dto.setEmail("john@mail.com");
        return dto;
    }
}