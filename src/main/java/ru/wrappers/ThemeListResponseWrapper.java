package ru.wrappers;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import ru.entities.Theme;

import java.util.List;

@Schema(description = "Ответ от API, содержащий список записей если таковые имеются. Содержит текст ошибки в слачае возникновения таковой.")
public class ThemeListResponseWrapper extends GenericResponseWrapper<List<Theme>>{

    public ThemeListResponseWrapper(List<Theme> data) {
        super(data);
    }

    public ThemeListResponseWrapper(String message) {
        super(message);
    }
}
