package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorHandlerTest {

    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void handleThrowable_shouldReturnErrorResponse() {
        ErrorResponse response = errorHandler.handleThrowable(new RuntimeException("Something went wrong"));

        assertThat(response.getError()).isEqualTo("Unexpected error");
        assertThat(response.getMessage()).isEqualTo("Something went wrong");
    }
}