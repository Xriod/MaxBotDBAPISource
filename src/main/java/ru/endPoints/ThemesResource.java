package ru.endPoints;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import ru.Constants;
import ru.wrappers.FAQListResponseWrapper;
import ru.wrappers.GenericResponseWrapper;
import ru.repositories.ThemeRepository;
import ru.wrappers.ThemeListResponseWrapper;
import ru.wrappers.ThemeResponseWrapper;

@Path("/DB/Themes")
@Tag(name = "Themes Resource", description = "Управление темами для FAQ и UserQuestions")
@Produces(MediaType.APPLICATION_JSON)
public class ThemesResource {

    @Inject
    ThemeRepository repo;

    //Возврат всех тем
    @GET
    @Path("/getAll")
    @Timeout(Constants.LIGHT_OPERATION_TIMEOUT_MILLS)
    @Operation(summary = "Получить список всех тем, отсортированных по алфавиту")
    @APIResponse(
            responseCode = "200",
            description = "Список всех тем",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ThemeListResponseWrapper.class),
                    example = """
                                {
                                    "data": [
                                        {
                                            "id": 10,
                                            "name": "Неделя первокурсников"
                                        },
                                        {
                                            "id": 1,
                                            "name": "Поступление"
                                        }
                                    ],
                                    "message": "success"
                                }
                    """
            )
    )
    @APIResponse(
            responseCode = "408",
            description = "Таймаут запроса",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ThemeListResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "Request timeout. Try again later."
                            }
                            """
            )
    )
    @APIResponse(
            responseCode = "500",
            description = "Внутренняя ошибка сервера или ошибка выполнения sql операции",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ThemeListResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "Unknown error: ErrorText"
                            }
                            """
            )
    )
    @APIResponse(
            responseCode = "503",
            description = "Ошибка подключения к БД",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ThemeListResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "SqlServer Exception occurred: Unable to connect to database"
                            }
                    """
            )
    )
    public Uni<Response> getAll() {
        return repo.getAllSorted().onItem().transform(themes ->
            Response.ok().entity(new ThemeListResponseWrapper(themes)).build());
    }

    //Удалить тему по id
    @DELETE
    @Path("/delete/{id}")
    @Timeout(Constants.LIGHT_OPERATION_TIMEOUT_MILLS)
    @Operation(summary = "Удалить тему по ID")
    @Parameter(
            name = "id",
            description = "Id темы",
            required = true,
            example = "3",
            schema = @Schema(implementation = Integer.class)
    )
    @APIResponse(
            responseCode = "200",
            description = "Тема успешно удалена",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ThemeResponseWrapper.class),
                    example = """
                           {
                                "data": null,
                                "message": "success"
                           }
                    """
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Некорректный Id (<=0)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ThemeResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "Id must be a positive number"
                            }
                    """
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "Тема не найдена",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ThemeResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "Theme not found"
                            }
                    """
            )
    )
    @APIResponse(
            responseCode = "408",
            description = "Таймаут запроса",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ThemeResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "Request timeout. Try again later."
                            }
                            """
            )
    )
    @APIResponse(
            responseCode = "500",
            description = "Внутренняя ошибка сервера или ошибка выполнения sql операции",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ThemeResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "Unknown error: ErrorText"
                            }
                            """
            )
    )
    @APIResponse(
            responseCode = "503",
            description = "Ошибка подключения к БД",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ThemeResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "SqlServer Exception occurred: Unable to connect to database"
                            }
                    """
            )
    )
    public Uni<Response> delete(@PathParam("id") Integer id) {
        return repo.deleteById(id).onItem().transform(v->
            Response.ok().entity(ThemeResponseWrapper.successWithNoData()).build()
        );
    }

    //Добавить тему
    @POST
    @Path("/add/{name}")
    @Timeout(Constants.LIGHT_OPERATION_TIMEOUT_MILLS)
    @Operation(summary = "Добавить новую тему")
    @Parameter(
            name = "name",
            description = "Название темы",
            required = true,
            example = "Поступление",
            schema = @Schema(implementation = String.class)
    )
    @APIResponse(
            responseCode = "200",
            description = "Тема успешно создана",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ThemeResponseWrapper.class),
                    example = """
                            {
                                "data": {
                                    "id": 11,
                                    "name": "Сессия"
                                },
                                "message": "success"
                            }
                    """
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Название пустое, null или слишком длинное",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ThemeResponseWrapper.class),
                    example = """
                        {
                                "data": null,
                                "message": "Name of theme can not be null or empty"
                        }
                    """
            )
    )
    @APIResponse(
            responseCode = "409",
            description = "Тема с таким названием уже существует",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ThemeResponseWrapper.class),
                    example = """
                         {
                                "data": null,
                                "message": "Theme with such name already exist"
                         }
                    """
            )
    )
    @APIResponse(
            responseCode = "408",
            description = "Таймаут запроса",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FAQListResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "Request timeout. Try again later."
                            }
                            """
            )
    )
    @APIResponse(
            responseCode = "500",
            description = "Внутренняя ошибка сервера или ошибка выполнения sql операции",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ThemeResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "Unknown error: ErrorText"
                            }
                            """
            )
    )
    @APIResponse(
            responseCode = "503",
            description = "Ошибка подключения к БД",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ThemeResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "SqlServer Exception occurred: Unable to connect to database"
                            }
                    """
            )
    )
    public Uni<Response> addTheme(@PathParam("name") String name) {
        return repo.addTheme(name).onItem()
                .transform(theme ->
                    Response.ok().entity(new ThemeResponseWrapper(theme)).build()
                );
    }
}
