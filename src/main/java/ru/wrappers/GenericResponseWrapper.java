package ru.wrappers;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Стандартная обёртка для ответов API. Содержит данные (data) и сообщение (message).")
public class GenericResponseWrapper<T> {

    @Schema(description = "Основные данные ответа. Может быть null, если операция не возвращает данных (например, DELETE) или вознила ошибка." +
            "В случае если есть данные возвращает либо 1 объект, либо массив если того подразумевает ответ.")
    public T data;

    @Schema(
            description = "Сообщение о результате операции. Содержит текст ошибки в случае её возникновения, в случае успешного выполнения: success ",
            required = true
    )
    public String message;

    public GenericResponseWrapper(T data) {
        this.data = data;
        message = "success";
    }

    public GenericResponseWrapper(String message) {
        this.message = message;
    }

    public static GenericResponseWrapper<?> unknownError(String message) {
        return new GenericResponseWrapper<>("Unknown error: " + message);
    }

    public static GenericResponseWrapper<?> successWithNoData() {
        return new GenericResponseWrapper<>("success");
    }

    public static GenericResponseWrapper<?> sqlServerException(String message) {
        return new GenericResponseWrapper<>("SqlServer Exception occurred: "+message);
    }

    public static GenericResponseWrapper<?> customResponse(String message) {
        return new GenericResponseWrapper<>(message);
    }
}
