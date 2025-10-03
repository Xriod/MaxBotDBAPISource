package ru.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import ru.Constants;

@Schema(description = "DTO для отправки вопроса от пользователя")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserQuestionDTO {

    @Schema(
            description = "Id пользователя, задающего вопрос",
            required = true,
            minimum = "1"
    )
    @NotNull(message = "Id can not be null")
    @Min(value = 1, message = "Id must be positive number")
    public Long id;

    @Schema(
            description = "Текст вопроса пользователя",
            required = true,
            minLength = 1,
            maxLength = Constants.MAX_QUESTION_LENGTH
    )
    @NotBlank(message = "Question can not be blank")
    @Size(max= Constants.MAX_QUESTION_LENGTH, message = "Question cannot exceed "+Constants.MAX_QUESTION_LENGTH+" characters")
    public String question;

    public UserQuestionDTO() {}
}
