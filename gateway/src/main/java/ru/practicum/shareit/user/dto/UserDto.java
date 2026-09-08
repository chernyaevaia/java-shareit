package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.common.Create;
import ru.practicum.shareit.common.Update;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;

    @NotBlank(groups = Create.class, message = "Name must not be blank")
    private String name;

    @NotBlank(groups = Create.class, message = "Email must not be blank")
    @Email(groups = {Create.class, Update.class}, message = "Email must be valid")
    private String email;
}