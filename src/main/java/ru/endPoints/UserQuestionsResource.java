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
import ru.dto.UserQuestionDTO;
import ru.repositories.UserQuestionsRepository;
import ru.wrappers.FAQListResponseWrapper;
import ru.wrappers.UserQuestionListResponseWrapper;
import ru.wrappers.UserQuestionResponseWrapper;

@Path("/DB/userQuestions")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "User Questions", description = "Управление вопросами, отправленными пользователями")
public class UserQuestionsResource {

    @Inject
    UserQuestionsRepository repo;

    @POST
    @Path("/post")
    @Consumes(MediaType.APPLICATION_JSON)
    @Timeout(Constants.HEAVY_OPERATION_TIMEOUT_MILLS)
    @Bulkhead(value = 5, waitingTaskQueue = 20)
    @CircuitBreaker(
            requestVolumeThreshold = 5,
            failureRatio = 0.7,
            delay = 5000,
            successThreshold = 2
    )
    @Operation(summary = "Отправить новый вопрос от пользователя")
    @RequestBody(
            description = "Тело запроса принимает json файл по шаблону определенном в примере.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserQuestionDTO.class),
                    example = """
                            {
                                "id":17,
                                "question":"Как к вам поступить?"
                            }
                    """
            )

    )
    @APIResponse(
            responseCode = "200",
            description = "Вопрос успешно сохранён",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserQuestionResponseWrapper.class),
                    example = """
                            {
                                "data": {
                                    "id": 5,
                                    "question": "Как можно отчислиться?",
                                    "answer": null,
                                    "questionCreationDate": "2025-09-21T15:42:34.0239485"
                                },
                                "message": "success"
                            }
                    """
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Ошибка валидации (пустой вопрос, некорректный Id)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserQuestionResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "Question can not be blank"
                            }
                    """
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserQuestionResponseWrapper.class),
                    example = """
                           {
                                "data": null,
                                "message": "Target user not found"
                           }
                    """
            )
    )
    @APIResponse(
            responseCode = "408",
            description = "Таймаут запроса",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserQuestionResponseWrapper.class),
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
                    "пока не освободится очередь и сервис не 'остынет'.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema( implementation = UserQuestionResponseWrapper.class),
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
                    schema = @Schema(implementation = UserQuestionResponseWrapper.class),
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
                    schema = @Schema(implementation = UserQuestionResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "SqlServer Exception occurred: Unable to connect to database"
                            }
                    """
            )
    )
    public Uni<Response> postQuestion(@Valid @NotNull(message = "Target json not found") UserQuestionDTO question) {
        return repo.addNewQuestion(question.id, question.question).onItem()
                .transform(res->Response.ok().entity(new UserQuestionResponseWrapper(res)).build());
    }

    @GET
    @Path("/getAllByUser/{id}")
    @Timeout(Constants.LIGHT_OPERATION_TIMEOUT_MILLS)
    @Operation(summary = "Получить все вопросы, заданные пользователем")
    @Parameter(
            name = "id",
            description = "Id пользователя",
            required = true,
            example = "777123456",
            schema = @Schema(implementation = Long.class)
    )
    @APIResponse(
            responseCode = "200",
            description = "Список вопросов пользователя",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserQuestionListResponseWrapper.class),
                    example = """
                            {
                                "data": [
                                    {
                                        "id": 5,
                                        "question": "Как можно отчислиться?",
                                        "answer": null,
                                        "questionCreationDate": "2025-09-21T15:42:34.0239485"
                                    }
                                ],
                                "message": "success"
                            }
                    """
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Некорректный Id пользователя(не число, <=0). Пример содержит ответ от запроса отправленного без Id.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserQuestionListResponseWrapper.class),
                    example = """
                        {
                            "data": null,
                            "message": "Unable to find matching target resource method"
                        }
                    """
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserQuestionListResponseWrapper.class),
                    example = """
                           {
                                "data": null,
                                "message": "Target user not found"
                           }
                    """
            )
    )
    @APIResponse(
            responseCode = "408",
            description = "Таймаут запроса",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserQuestionListResponseWrapper.class),
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
                    schema = @Schema(implementation = UserQuestionListResponseWrapper.class),
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
                    schema = @Schema(implementation = UserQuestionListResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "SqlServer Exception occurred: Unable to connect to database"
                            }
                    """
            )
    )
    public Uni<Response> getAllForUser(@PathParam("id") Long id) {
        return repo.getAllQuestions(id).onItem()
                .transform(res->Response.ok().entity(new UserQuestionListResponseWrapper(res)).build());
    }

    @DELETE
    @Path("/removeByUser/{id}")
    @Timeout(Constants.LIGHT_OPERATION_TIMEOUT_MILLS)
    @Operation(summary = "Удалить все вопросы заданные пользователем")
    @Parameter(
            name = "id",
            description = "Id пользователя",
            required = true,
            example = "2760",
            schema = @Schema(implementation = Long.class)
    )
    @APIResponse(
            responseCode = "200",
            description = "Вопросы пользователя успешно удалены",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserQuestionListResponseWrapper.class),
                    example = """
                            {
                                "data":null,
                                "message": "success"
                            }
                    """
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Некорректный Id пользователя(не число, <=0). Пример содержит ответ от запроса отправленного без Id.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserQuestionResponseWrapper.class),
                    example = """
                        {
                            "data": null,
                            "message": "Unable to find matching target resource method"
                        }
                    """
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserQuestionResponseWrapper.class),
                    example = """
                           {
                                "data": null,
                                "message": "Target user not found"
                           }
                    """
            )
    )
    @APIResponse(
            responseCode = "408",
            description = "Таймаут запроса",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserQuestionResponseWrapper.class),
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
                    schema = @Schema(implementation = UserQuestionResponseWrapper.class),
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
                    schema = @Schema(implementation = UserQuestionResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "SqlServer Exception occurred: Unable to connect to database"
                            }
                    """
            )
    )
    public Uni<Response> removeByUser(@PathParam("id") Long id) {
        return repo.removeAllQuestionsByUser(id).onItem()
                .transform(res->Response.ok().entity(UserQuestionResponseWrapper.successWithNoData()).build());
    }
}
