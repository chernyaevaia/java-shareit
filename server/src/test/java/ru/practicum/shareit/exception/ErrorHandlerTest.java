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
    void handleNotFoundException_withNullMessage_shouldReturnErrorResponse() {
        ErrorResponse response = errorHandler.handleNotFoundException(new NotFoundException(null));
        assertThat(response.getError()).isEqualTo("Not found");
        assertThat(response.getDescription()).isNull();
    }

    @Test
    void handleNotFoundException_withEmptyMessage_shouldReturnErrorResponse() {
        ErrorResponse response = errorHandler.handleNotFoundException(new NotFoundException(""));
        assertThat(response.getError()).isEqualTo("Not found");
        assertThat(response.getDescription()).isEmpty();
    }

    @Test
    void handleForbiddenException_shouldReturnErrorResponse() {
        ErrorResponse response = errorHandler.handleForbiddenException(new ForbiddenException("Access denied"));
        assertThat(response.getError()).isEqualTo("Forbidden");
        assertThat(response.getDescription()).isEqualTo("Access denied");
    }

    @Test
    void handleForbiddenException_withNullMessage_shouldReturnErrorResponse() {
        ErrorResponse response = errorHandler.handleForbiddenException(new ForbiddenException(null));
        assertThat(response.getError()).isEqualTo("Forbidden");
        assertThat(response.getDescription()).isNull();
    }

    @Test
    void handleValidationException_shouldReturnErrorResponse() {
        ErrorResponse response = errorHandler.handleValidationException(new ValidationException("Invalid input"));
        assertThat(response.getError()).isEqualTo("Validation error");
        assertThat(response.getDescription()).isEqualTo("Invalid input");
    }

    @Test
    void handleValidationException_withNullMessage_shouldReturnErrorResponse() {
        ErrorResponse response = errorHandler.handleValidationException(new ValidationException(null));
        assertThat(response.getError()).isEqualTo("Validation error");
        assertThat(response.getDescription()).isNull();
    }

    @Test
    void handleConflictException_shouldReturnErrorResponse() {
        ErrorResponse response = errorHandler.handleConflictException(new ConflictException("Email exists"));
        assertThat(response.getError()).isEqualTo("Conflict");
        assertThat(response.getDescription()).isEqualTo("Email exists");
    }

    @Test
    void handleConflictException_withNullMessage_shouldReturnErrorResponse() {
        ErrorResponse response = errorHandler.handleConflictException(new ConflictException(null));
        assertThat(response.getError()).isEqualTo("Conflict");
        assertThat(response.getDescription()).isNull();
    }

    @Test
    void handleThrowable_shouldReturnErrorResponse() {
        ErrorResponse response = errorHandler.handleThrowable(new RuntimeException("Something went wrong"));
        assertThat(response.getError()).isEqualTo("Unexpected error");
        assertThat(response.getDescription()).isEqualTo("Something went wrong");
    }

    @Test
    void handleThrowable_withNullMessage_shouldReturnErrorResponse() {
        ErrorResponse response = errorHandler.handleThrowable(new RuntimeException((String) null));
        assertThat(response.getError()).isEqualTo("Unexpected error");
        assertThat(response.getDescription()).isNull();
    }

    @Test
    void handleThrowable_withIllegalArgumentException_shouldReturnErrorResponse() {
        ErrorResponse response = errorHandler.handleThrowable(new IllegalArgumentException("Invalid argument"));
        assertThat(response.getError()).isEqualTo("Unexpected error");
        assertThat(response.getDescription()).isEqualTo("Invalid argument");
    }

    @Test
    void handleThrowable_withNullPointerException_shouldReturnErrorResponse() {
        ErrorResponse response = errorHandler.handleThrowable(new NullPointerException("Null value"));
        assertThat(response.getError()).isEqualTo("Unexpected error");
        assertThat(response.getDescription()).isEqualTo("Null value");
    }

    @Test
    void handleThrowable_withLongMessage_shouldReturnErrorResponse() {
        String longMessage = "A".repeat(1000);
        ErrorResponse response = errorHandler.handleThrowable(new RuntimeException(longMessage));
        assertThat(response.getError()).isEqualTo("Unexpected error");
        assertThat(response.getDescription()).isEqualTo(longMessage);
        assertThat(response.getDescription()).hasSize(1000);
    }

    @Test
    void handleThrowable_withSpecialCharacters_shouldReturnErrorResponse() {
        String specialMessage = "Error: <>&\"'@#$%^&*()";
        ErrorResponse response = errorHandler.handleThrowable(new RuntimeException(specialMessage));
        assertThat(response.getError()).isEqualTo("Unexpected error");
        assertThat(response.getDescription()).isEqualTo(specialMessage);
    }

    @Test
    void errorResponse_shouldBeImmutable() {
        ErrorResponse response = errorHandler.handleNotFoundException(new NotFoundException("Test"));
    
        assertThat(response.getError()).isNotNull();
        assertThat(response.getDescription()).isNotNull();
    
        assertThat(response.toString()).contains("Not found");
    }
}