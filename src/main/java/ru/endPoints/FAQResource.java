package ru.endPoints;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import ru.Constants;
import ru.wrappers.FAQListResponseWrapper;
import ru.wrappers.FAQResponseWrapper;
import ru.dto.FAQDTO;
import ru.dto.FAQFullDTO;
import ru.repositories.FAQRepository;

@Path("/DB/FAQ")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "FAQ Resource", description = "Управление часто задаваемыми вопросами")
public class FAQResource {

    @Inject
    FAQRepository repo;

    @GET
    @Path("/getAllByTheme/{id}")
    @Timeout(Constants.LIGHT_OPERATION_TIMEOUT_MILLS)
    @Bulkhead(value = 5, waitingTaskQueue = 20)
    @CircuitBreaker(
            requestVolumeThreshold = 5,
            failureRatio = 0.7,
            delay = 5000,
            successThreshold = 2
    )
    @Operation(summary = "Получить все FAQ по ID темы",
                description = "Возвращает список FAQ в виде json массива в обёртке."
    )
    @Parameter(
            name = "id",
            description = "Id темы",
            required = true,
            example = "5",
            schema = @Schema(implementation = Integer.class)
    )
    @RequestBody(required = false)
    @APIResponse(
            responseCode = "200",
            description = "Список FAQ по теме",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FAQListResponseWrapper.class),
                    example = """
                            {
                                "data": [
                                    {
                                        "id": 3,
                                        "question": "Как к вам поступить?",
                                        "answer": "Подать документы через портал государственных услуг.",
                                        "theme": "Поступление"
                                    },
                                    {
                                        "id": 4,
                                        "question": "Солько длится семестр?",
                                        "answer": "Половину учебного года.",
                                        "theme": "Поступление"
                                    }
                                ],
                                "message": "success"
                            }
            """
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Некорректный Id темы (число <=0). Иное поведение при вводе текста.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FAQListResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "Theme Id cannot be null or negative"
                            }
                            """
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "Тема указанная в запросе не найдена",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FAQListResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "Theme with Id = 3 not found"
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
            responseCode = "429",
            description = "Слишком много запросов. Данный endpoint может испытывать высокие нагрзки." +
                    "В случае возникновения таких он закроется, будет принимать половину или вовсе не принимать запросы," +
                    "пока не освободится очередь.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema( implementation = FAQListResponseWrapper.class),
                    example = """
                        {
                           "data": null,
                           "message": "Service experience high loads. Try again later."
                        }
                    """
            )
    )
    @APIResponse(
            responseCode = "500",
            description = "Внутренняя ошибка сервера или ошибка выполнения sql операции",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FAQListResponseWrapper.class),
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
                    schema = @Schema(implementation = FAQListResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "SqlServer Exception occurred: Unable to connect to database"
                            }
                    """
            )
    )
    public Uni<Response> getAllByTheme(@PathParam("id") Integer id) {
        return repo.findByThemeId(id).onItem()
                .transform(res->Response.ok().entity(new FAQListResponseWrapper(res)).build());
    }

    @DELETE
    @Path("/delete/{id}")
    @Timeout(Constants.LIGHT_OPERATION_TIMEOUT_MILLS)
    @Operation(summary = "Удалить FAQ по Id")
    @Parameter(
            name = "id",
            description = "Id записи FAQ",
            required = true,
            example = "101",
            schema = @Schema(implementation = Long.class)
    )
    @APIResponse(
            responseCode = "200",
            description = "FAQ успешно удалён",
            content = @Content(
                    schema = @Schema(implementation = FAQResponseWrapper.class),
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
            description = "Некорректный Id (0<=)",
            content = @Content(
                    schema = @Schema(implementation = FAQResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "Id cannot be null or empty"
                            }
                            """
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "FAQ не найден",
            content = @Content(
                    schema = @Schema(implementation = FAQResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "FAQ with Id = 5 not found"
                            }
                            """
            )
    )
    @APIResponse(
            responseCode = "408",
            description = "Таймаут запроса",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FAQResponseWrapper.class),
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
                    schema = @Schema(implementation = FAQResponseWrapper.class),
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
                    schema = @Schema(implementation = FAQResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "SqlServer Exception occurred: Unable to connect to database"
                            }
                    """
            )
    )
    public Uni<Response> deleteFAQ(@PathParam("id") Long id) {
        return repo.deleteFAQ(id).onItem()
                        .transform(res->Response.ok().entity(FAQResponseWrapper.successWithNoData()).build());
    }

    @POST
    @Path("/addFAQ")
    @Consumes(MediaType.APPLICATION_JSON)
    @Timeout(Constants.HEAVY_OPERATION_TIMEOUT_MILLS)
    @Operation(summary = "Добавить новый FAQ")
    @RequestBody(
            description = "Тело запроса должно содержать json файл определенного в примере вида.",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FAQDTO.class),
                    example = """        
                            {
                               "question":"Как к вам поступить?",
                               "answer":"Подать документы через портал госудерственных услуг",
                               "themeId":5
                            }
                    """
            )
    )
    @APIResponse(
            responseCode = "200",
            description = "FAQ успешно создан",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FAQResponseWrapper.class),
                    example = """
                            {
                                "data": {
                                    "id": 7,
                                    "question": "Как к вам поступить?",
                                    "answer": "Подать документы через портал государственных услуг.",
                                    "theme": "Поступление"
                                },
                                "message": "success"
                            }
                    """
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Ошибка валидации входных данных. Пример показывает запрос с отсутствующим json." +
                    "В случае оствуствия каких-либо параметров, поменяется текст ошибки, код ответа сохранится.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FAQResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "Target json not found"
                            }
                    """
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "Тема по указанному в запросе Id не найдена не найдена",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FAQResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "Theme with Id = 5 not found"
                            }
                    """
            )
    )
    @APIResponse(
            responseCode = "408",
            description = "Таймаут запроса",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FAQResponseWrapper.class),
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
                    schema = @Schema(implementation = FAQResponseWrapper.class),
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
                    schema = @Schema(implementation = FAQResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "SqlServer Exception occurred: Unable to connect to database"
                            }
                    """
            )
    )
    public Uni<Response> addFAQ(@Valid @NotNull(message = "Target json not found") FAQDTO faq) {
        return repo.addNewFAQ(faq.question, faq.answer, faq.themeId).onItem()
                .transform(res->Response.ok().entity(new FAQResponseWrapper(res)).build());
    }

    @PATCH
    @Path("/updateFAQ")
    @Consumes(MediaType.APPLICATION_JSON)
    @Timeout(Constants.HEAVY_OPERATION_TIMEOUT_MILLS)
    @Operation(summary = "Обновить FAQ")
    @RequestBody(
            description = "Запрос на обновление FAQ записи, прикладывается полная информация по записи в json. " +
                          "Тело запроса должно содержать файл определенного в примере вида.",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FAQFullDTO.class),
                    example = """        
                             {
                                 "id":17,
                                 "question":"Как к вам поступить?",
                                 "answer":"Подать документы через портал госудерственных услуг",
                                 "themeId":5
                             }
                    """
            )
    )
    @APIResponse(
            responseCode = "200",
            description = "FAQ успешно создан",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FAQResponseWrapper.class),
                    example = """
                    {
                                "data": {
                                    "id": 7,
                                    "question": "Как к вам поступить?",
                                    "answer": "Подать документы через портал государственных услуг.",
                                    "theme": "Поступление"
                                },
                                "message": "success"
                    }
                    """
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Ошибка валидации входных данных (пустые поля, слишком длинные строки и т.д.)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FAQResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "Id can not be null; Theme ID cannot be null"
                            }
                    """
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "Тема или FAQ по предоставленному Id не найдена",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FAQResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "FAQ with Id = 5 not found"
                            }
                    """
            )
    )
    @APIResponse(
            responseCode = "408",
            description = "Таймаут запроса",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = FAQResponseWrapper.class),
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
                    schema = @Schema(implementation = FAQResponseWrapper.class),
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
                    schema = @Schema(implementation = FAQResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "SqlServer Exception occurred: Unable to connect to database"
                            }
                    """
            )
    )
    public Uni<Response> updateFAQ(@Valid @NotNull(message ="Target json not found") FAQFullDTO faq) {
        return repo.updateFAQ(faq.id, faq.question, faq.answer, faq.themeId).onItem()
                .transform(res->Response.ok().entity(new FAQResponseWrapper(res)).build());
    }


}
