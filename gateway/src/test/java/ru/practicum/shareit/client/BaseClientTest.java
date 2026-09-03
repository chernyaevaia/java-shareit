package ru.practicum.shareit.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class BaseClientTest {

    private final RestTemplate restTemplate = new RestTemplate();
    private final MockRestServiceServer server = MockRestServiceServer.createServer(restTemplate);
    private final BaseClient client = new BaseClient(restTemplate);

    @Test
    void get_shouldSendGetRequest() {
        server.expect(requestTo("/test"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.get("/test");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }

    @Test
    void post_shouldSendPostRequest() {
        server.expect(requestTo("/test"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        UserDto body = new UserDto();
        body.setName("John");
        body.setEmail("john@mail.com");

        ResponseEntity<Object> response = client.post("/test", body);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }

    @Test
    void get_withUserId_shouldIncludeHeader() {
        server.expect(requestTo("/test"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.get("/test", 1L);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        server.verify();
    }

    @Test
    void get_serverError_shouldReturnErrorStatus() {
        server.expect(requestTo("/test"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withBadRequest());

        ResponseEntity<Object> response = client.get("/test");

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        server.verify();
    }
}