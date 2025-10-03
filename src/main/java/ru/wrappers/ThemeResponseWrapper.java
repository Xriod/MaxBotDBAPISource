package ru.wrappers;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import ru.entities.Theme;

@Schema(description = "Ответ от API, содержащий единственную если таковая имеется. Содержит текст ошибки в слачае возникновения таковой.")
public class ThemeResponseWrapper extends GenericResponseWrapper<Theme>{

    public ThemeResponseWrapper(Theme data) {
        super(data);
    }

    public ThemeResponseWrapper(String message) {
        super(message);
    }
}
