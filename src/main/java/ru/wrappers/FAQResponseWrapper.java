package ru.wrappers;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import ru.entities.FAQ;

@Schema(description = "Ответ от API, содержащий единственную запись если таковая имеется. Содержит текст ошибки в слачае возникновения таковой.")
public class FAQResponseWrapper extends GenericResponseWrapper<FAQ> {

    public FAQResponseWrapper(FAQ data) {
        super(data);
    }

    public FAQResponseWrapper(String message) {
        super(message);
    }
}
