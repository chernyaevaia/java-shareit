package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void serialize_shouldFormatDatesAsIso() throws Exception {
        BookingDto dto = new BookingDto();
        dto.setId(1L);
        dto.setStart(LocalDateTime.of(2025, 1, 1, 12, 30, 15));
        dto.setEnd(LocalDateTime.of(2025, 1, 2, 12, 30, 15));
        dto.setStatus(BookingStatus.WAITING);

        JsonContent<BookingDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2025-01-01T12:30:15");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2025-01-02T12:30:15");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
    }

    @Test
    void deserialize_shouldParseIsoDates() throws Exception {
        String content = "{\"id\":1,\"start\":\"2025-01-01T12:30:15\",\"end\":\"2025-01-02T12:30:15\",\"status\":\"WAITING\"}";

        BookingDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2025, 1, 1, 12, 30, 15));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2025, 1, 2, 12, 30, 15));
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.WAITING);
    }
}