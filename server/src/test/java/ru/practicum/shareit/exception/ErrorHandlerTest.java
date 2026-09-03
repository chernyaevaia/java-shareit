package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorHandlerTest {

    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void handleNotFoundException_shouldReturnErrorResponse() {
        ErrorResponse response = errorHandler.handleNotFoundException(new NotFoundException("User not found"));
        assertThat(response.getError()).isEqualTo("Not found");
        assertThat(response.getDescription()).isEqualTo("User not found");
    }

    @Test
    void handleForbiddenException_shouldReturnErrorResponse() {
        ErrorResponse response = errorHandler.handleForbiddenException(new ForbiddenException("Access denied"));
        assertThat(response.getError()).isEqualTo("Forbidden");
        assertThat(response.getDescription()).isEqualTo("Access denied");
    }

    @Test
    void handleValidationException_shouldReturnErrorResponse() {
        ErrorResponse response = errorHandler.handleValidationException(new ValidationException("Invalid input"));
        assertThat(response.getError()).isEqualTo("Validation error");
        assertThat(response.getDescription()).isEqualTo("Invalid input");
    }

    @Test
    void handleConflictException_shouldReturnErrorResponse() {
        ErrorResponse response = errorHandler.handleConflictException(new ConflictException("Email exists"));
        assertThat(response.getError()).isEqualTo("Conflict");
        assertThat(response.getDescription()).isEqualTo("Email exists");
    }

    @Test
    void handleThrowable_shouldReturnErrorResponse() {
        ErrorResponse response = errorHandler.handleThrowable(new RuntimeException("Something went wrong"));
        assertThat(response.getError()).isEqualTo("Unexpected error");
        assertThat(response.getDescription()).isEqualTo("Something went wrong");
    }
}