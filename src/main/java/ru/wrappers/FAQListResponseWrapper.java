package ru.wrappers;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import ru.entities.FAQ;

import java.util.List;

@Schema(description = "Ответ от API, содержащий список записей если таковые имеются. Содержит текст ошибки в слачае возникновения таковой.")
public class FAQListResponseWrapper extends GenericResponseWrapper<List<FAQ>> {

    public FAQListResponseWrapper(List<FAQ> data) {
        super(data);
    }

    public FAQListResponseWrapper(String message) {
        super(message);
    }

}
