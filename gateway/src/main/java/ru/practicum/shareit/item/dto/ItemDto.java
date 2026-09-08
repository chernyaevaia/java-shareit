package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.common.Create;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    private Long id;

    @NotBlank(groups = Create.class, message = "Name must not be blank")
    private String name;

    @NotBlank(groups = Create.class, message = "Description must not be blank")
    private String description;

    @NotNull(groups = Create.class, message = "Available must not be null")
    private Boolean available;

    private Long requestId;
}