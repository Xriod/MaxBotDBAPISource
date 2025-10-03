package ru.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import ru.Constants;

@Schema(description = "Data Transfer Object для создания новой записи FAQ")
@JsonIgnoreProperties(ignoreUnknown = true)
public class FAQDTO {

    @Schema(
            description = "Текст вопроса",
            required = true,
            minLength = 1,
            maxLength = Constants.MAX_QUESTION_LENGTH
    )
    @NotBlank(message = "Question can not be blank")
    @Size(max= Constants.MAX_QUESTION_LENGTH, message = "Question cannot exceed "+Constants.MAX_QUESTION_LENGTH+" characters")
    public String question;

    @Schema(
            description = "Текст ответа",
            required = true,
            minLength = 1,
            maxLength = Constants.MAX_ANSWER_LENGTH
    )
    @NotBlank(message = "Answer can not be blank")
    @Size(max=Constants.MAX_ANSWER_LENGTH, message = "Answer cannot exceed "+Constants.MAX_ANSWER_LENGTH+" characters")
    public String answer;

    @Schema(
            description = "ID темы, к которой относится вопрос",
            required = true,
            minimum = "1"
    )
    @NotNull(message = "Theme ID cannot be null")
    @Min(value = 1,message = "Theme ID must be positive")
    public Integer themeId;

    public FAQDTO() {}
}
