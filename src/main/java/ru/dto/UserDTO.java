package ru.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import ru.Constants;

@Schema(description = "DTO для добавления нового пользователя")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {

    @Schema(
            description = "Уникальный идентификатор пользователя (MAX_ID)",
            required = true,
            minimum = "1"
    )
    @NotNull(message = "Id can not be null")
    @Min(value = 1, message = "Id must be positive number")
    public Long id;

    @Schema(
            description = "Отображаемое имя пользователя",
            maxLength = Constants.MAX_USERNAME_LENGTH
    )
    @Size(max= Constants.MAX_USERNAME_LENGTH, message = "UserName cannot exceed "+Constants.MAX_USERNAME_LENGTH+" characters")
    public String maxName;

    public UserDTO() {}
}
