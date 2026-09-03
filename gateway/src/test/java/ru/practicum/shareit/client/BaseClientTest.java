package ru.practicum.shareit.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BaseClientTest {

    private RestTemplate restTemplate;
    private BaseClient baseClient;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        baseClient = new BaseClient(restTemplate);
    }

    @Test
    void get_withOnlyPath_shouldCallExchangeWithoutUserId() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("test");
        when(restTemplate.exchange(eq("/test"), eq(HttpMethod.GET), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.get("/test");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("test");
    }

    @Test
    void post_withOnlyPathAndBody_shouldCallExchangeWithoutUserId() {
        String body = "test body";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("created");
        when(restTemplate.exchange(eq("/test"), eq(HttpMethod.POST), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.post("/test", body);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void put_shouldCallExchangeWithPutMethod() {
        String body = "update body";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("updated");
        when(restTemplate.exchange(eq("/test"), eq(HttpMethod.PUT), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.put("/test", 1L, body);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void delete_withUserId_shouldCallExchangeWithDeleteMethod() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.noContent().build();
        when(restTemplate.exchange(eq("/test"), eq(HttpMethod.DELETE), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.delete("/test", 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void makeAndSendRequest_withParameters_shouldPassParametersToExchange() {
        Map<String, Object> parameters = Map.of("param1", "value1", "param2", "value2");
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("test");
        when(restTemplate.exchange(eq("/test"), eq(HttpMethod.GET), any(), eq(Object.class), eq(parameters)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.get("/test", 1L, parameters);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void makeAndSendRequest_whenHttpStatusCodeException_shouldReturnErrorResponse() {
        HttpClientErrorException exception = new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Error message".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = baseClient.get("/test");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Error message".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void prepareGatewayResponse_withNonSuccessStatusAndBody_shouldReturnErrorWithBody() {
        ResponseEntity<Object> serverResponse = ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class)))
                .thenReturn(serverResponse);

        ResponseEntity<Object> response = baseClient.get("/test");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Not found");
    }

    @Test
    void prepareGatewayResponse_withNonSuccessStatusAndNoBody_shouldReturnErrorWithoutBody() {
        ResponseEntity<Object> serverResponse = ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Object.class)))
                .thenReturn(serverResponse);

        ResponseEntity<Object> response = baseClient.get("/test");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void patch_withOnlyPathAndBody_shouldCallExchange() {
        String body = "patch body";
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("patched");
        when(restTemplate.exchange(eq("/test"), eq(HttpMethod.PATCH), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.patch("/test", body);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void post_withUserIdAndParameters_shouldIncludeUserIdInHeaders() {
        Map<String, Object> parameters = Map.of("key", "value");
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("created");
        when(restTemplate.exchange(eq("/test"), eq(HttpMethod.POST), any(), eq(Object.class), eq(parameters)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.post("/test", 1L, parameters, "body");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void delete_withOnlyPath_shouldCallExchange() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.noContent().build();
        when(restTemplate.exchange(eq("/test"), eq(HttpMethod.DELETE), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.delete("/test");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}