package ru.wrappers;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import ru.entities.UserQuestion;

@Schema(description = "Ответ от API, содержащий единственную если таковая имеется. Содержит текст ошибки в слачае возникновения таковой.")
public class UserQuestionResponseWrapper extends GenericResponseWrapper<UserQuestion> {

    public UserQuestionResponseWrapper(UserQuestion data) {
        super(data);
    }

    public UserQuestionResponseWrapper(String message) {
        super(message);
    }
}
