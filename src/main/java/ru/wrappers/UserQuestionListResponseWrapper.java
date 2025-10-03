package ru.wrappers;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import ru.entities.UserQuestion;

import java.util.List;
@Schema(description = "Ответ от API, содержащий список записей если таковые имеются. Содержит текст ошибки в слачае возникновения таковой.")
public class UserQuestionListResponseWrapper extends GenericResponseWrapper<List<UserQuestion>> {

    public UserQuestionListResponseWrapper(List<UserQuestion> data) {
        super(data);
    }

    public UserQuestionListResponseWrapper(String message) {
        super(message);
    }
}
