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
import ru.Constants;
import ru.dto.UserDTO;
import ru.repositories.UserRepository;
import ru.wrappers.FAQListResponseWrapper;
import ru.wrappers.RoleResponseWrapper;
import ru.wrappers.UserResponseWrapper;


@Path("/DB/users")
@Produces(MediaType.APPLICATION_JSON)
public class UsersResource {

    @Inject
    UserRepository repo;

    //Добавление нового юзера
    @POST
    @Path("/addUser")
    @Consumes(MediaType.APPLICATION_JSON)
    @Timeout(Constants.HEAVY_OPERATION_TIMEOUT_MILLS)
    @Operation(
            summary = "Добавить нового пользователя",
            description = "Регистрирует нового пользователя в системе. Если пользователь уже существует — возвращает ошибку 409."
    )
    @RequestBody(
            description = "Данные нового пользователя. Имя пользователя - опционально. Поле должно присутствовать но его можно оставить пустыи, например '' ",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserDTO.class),
                    example = """
                            {
                               "id":17,
                               "maxName":""
                            }
                """
            )
    )
    @APIResponse(
            responseCode = "200",
            description = "Пользователь успешно создан, возвращается его роль",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = RoleResponseWrapper.class),
                    example = """
                            {
                                "data": {
                                    "id": 1,
                                    "name": "user"
                                },
                                "message": "success"
                            }
                """
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Ошибка валидации: отсутствует тело запроса, некорректный ID",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = RoleResponseWrapper.class),
                    example = """
                {
                  "data": null,
                  "message": "Target json not found; Id must be positive number"
                }
                """
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "Не найдена роль по умолчанию ('user')",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = RoleResponseWrapper.class),
                    example = """
                            {
                              "data": null,
                              "message": "Default user role not found"
                            }
                """
            )
    )
    @APIResponse(
            responseCode = "408",
            description = "Таймаут запроса",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = RoleResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "Request timeout. Try again later."
                            }
                            """
            )
    )
    @APIResponse(
            responseCode = "409",
            description = "Пользователь с таким ID уже существует",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = RoleResponseWrapper.class),
                    example = """
                            {
                              "data": null,
                              "message": "User already exists"
                            }
                """
            )
    )
    @APIResponse(
            responseCode = "500",
            description = "Внутренняя ошибка сервера или ошибка выполнения sql операции",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = RoleResponseWrapper.class),
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
                    schema = @Schema(implementation = RoleResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "SqlServer Exception occurred: Unable to connect to database"
                            }
                    """
            )
    )
    public Uni<Response> addUser(@Valid @NotNull(message = "Target json not found") UserDTO user) {
        return repo.addUser(user.id, user.maxName)
                .onItem().transform(resp->
                    Response.ok().entity(new RoleResponseWrapper(resp.getRole())).build()
                );
    }

    //Получение роли юзера по id
    @GET
    @Path("/getRole/{id}")
    @Timeout(Constants.LIGHT_OPERATION_TIMEOUT_MILLS)
    @Bulkhead(value = 5, waitingTaskQueue = 20)
    @CircuitBreaker(
            requestVolumeThreshold = 5,
            failureRatio = 0.7,
            delay = 5000,
            successThreshold = 2
    )
    @Operation(
            summary = "Получить роль пользователя по Id",
            description = "Возвращает текущую роль пользователя (например, 'user', 'admin', 'blocked')."
    )
    @Parameter(
            name = "id",
            description = "ID пользователя (MAX_ID)",
            required = true,
            example = "777123456",
            schema = @Schema(implementation = Long.class)
    )
    @RequestBody(
            required = false
    )
    @APIResponse(
            responseCode = "200",
            description = "Роль пользователя успешно получена",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = RoleResponseWrapper.class),
                    example = """
                            {
                              "data": {
                                "id": 1,
                                "name": "user"
                              },
                              "message": "success"
                            }
                """
            )
    )
    @APIResponse(
            responseCode = "400",
            description = "Некорректный Id (не число, <=0)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = RoleResponseWrapper.class),
                    example = """
                {
                  "data": null,
                  "message": "UserId must be positive and not null"
                }
                """
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = RoleResponseWrapper.class),
                    example = """
                            {
                              "data": null,
                              "message": "User not found"
                            }
                """
            )
    )
    @APIResponse(
            responseCode = "408",
            description = "Таймаут запроса",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = RoleResponseWrapper.class),
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
                    schema = @Schema( implementation = RoleResponseWrapper.class),
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
                    schema = @Schema(implementation = RoleResponseWrapper.class),
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
                    schema = @Schema(implementation = RoleResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "SqlServer Exception occurred: Unable to connect to database"
                            }
                    """
            )
    )
    public Uni<Response> getRole(@PathParam("id") Long id) {
        return repo.getUserRole(id).onItem()
                .transform(resp->
                    Response.ok().entity(new RoleResponseWrapper(resp)).build()
                );
    }

    @PATCH
    @Path("/promote/{id}")
    @Timeout(Constants.LIGHT_OPERATION_TIMEOUT_MILLS)
    @Operation(
            summary = "Повысить пользователя до администратора",
            description = "Изменяет роль пользователя на 'admin'."
    )
    @RequestBody(
            required = false
    )
    @Parameter(
            name = "id",
            description = "Id пользователя",
            required = true,
            example = "32712",
            schema = @Schema(implementation = Long.class)
    )
    @APIResponse(
            responseCode = "200",
            description = "Пользователь успешно повышен до администратора",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserResponseWrapper.class),
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
            description = "Некорректный Id. Некорректные входные данные.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserResponseWrapper.class),
                    example = """
                            {
                              "data": null,
                              "message": "UserId must be positive and not null"
                            }
                """
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "Пользователь не найден или не найдена роль 'admin'",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserResponseWrapper.class),
                    example = """
                                {
                                  "data": null,
                                  "message": "User not found"
                                }
                """
            )
    )
    @APIResponse(
            responseCode = "408",
            description = "Таймаут запроса",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserResponseWrapper.class),
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
                    schema = @Schema(implementation = UserResponseWrapper.class),
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
                    schema = @Schema(implementation = UserResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "SqlServer Exception occurred: Unable to connect to database"
                            }
                    """
            )
    )
    public Uni<Response> promoteUser(@PathParam("id") Long id) {
        return repo.promoteUser(id).onItem()
                .transform(resp->
                    Response.ok().entity(UserResponseWrapper.successWithNoData()).build()
                );
    }

    @PATCH
    @Path("/demote/{id}")
    @Timeout(Constants.LIGHT_OPERATION_TIMEOUT_MILLS)
    @Operation(
            summary = "Понизить пользователя до обычного юзера",
            description = "Изменяет роль пользователя на 'user'."
    )
    @Parameter(
            name = "id",
            description = "Id пользователя",
            required = true,
            example = "315267",
            schema = @Schema(implementation = Long.class)
    )
    @APIResponse(
            responseCode = "200",
            description = "Пользователь успешно понижен",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserResponseWrapper.class),
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
            description = "Некорректный Id. Некорретные входные данные",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserResponseWrapper.class),
                    example = """
                            {
                              "data": null,
                              "message": "UserId must be positive and not null"
                            }
                """
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "Пользователь не найден или не найдена роль 'user'",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserResponseWrapper.class),
                    example = """
                            {
                              "data": null,
                              "message": "User not found"
                            }
                """
            )
    )
    @APIResponse(
            responseCode = "408",
            description = "Таймаут запроса",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserResponseWrapper.class),
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
                    schema = @Schema(implementation = UserResponseWrapper.class),
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
                    schema = @Schema(implementation = UserResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "SqlServer Exception occurred: Unable to connect to database"
                            }
                    """
            )
    )
    public Uni<Response> demoteUser(@PathParam("id") Long id) {
        return repo.demoteUser(id).onItem()
                .transform(resp->
                    Response.ok().entity(UserResponseWrapper.successWithNoData()).build()
                );
    }

    //блокировать пользователя
    @PATCH
    @Path("/block/{id}")
    @Timeout(Constants.LIGHT_OPERATION_TIMEOUT_MILLS)
    @Operation(
            summary = "Заблокировать пользователя",
            description = "Устанавливает пользователю роль 'blocked', запрещая доступ к системе."
    )
    @Parameter(
            name = "id",
            description = "Id пользователя",
            required = true,
            example = "307504",
            schema = @Schema(implementation = Long.class)
    )
    @APIResponse(
            responseCode = "200",
            description = "Пользователь успешно заблокирован",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserResponseWrapper.class),
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
            description = "Некорректный Id. Некорректные входные данные.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserResponseWrapper.class),
                    example = """
                            {
                              "data": null,
                              "message": "UserId must be positive and not null"
                            }
                """
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "Пользователь не найден или не найдена роль 'blocked'",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserResponseWrapper.class),
                    example = """
                {
                  "data": null,
                  "message": "User not found"
                }
                """
            )
    )
    @APIResponse(
            responseCode = "408",
            description = "Таймаут запроса",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserResponseWrapper.class),
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
                    schema = @Schema(implementation = UserResponseWrapper.class),
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
                    schema = @Schema(implementation = UserResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "SqlServer Exception occurred: Unable to connect to database"
                            }
                    """
            )
    )
    public Uni<Response> blockUser(@PathParam("id") Long id) {
        return repo.blockUser(id).onItem()
                .transform(resp->
                        Response.ok().entity(UserResponseWrapper.successWithNoData()).build()
                );
    }

    //разблокировать пользователя
    @PATCH
    @Path("/unblock/{id}")
    @Timeout(Constants.LIGHT_OPERATION_TIMEOUT_MILLS)
    @Operation(
            summary = "Разблокировать пользователя",
            description = "Возвращает пользователю роль 'user', восстанавливая доступ."
    )
    @Parameter(
            name = "id",
            description = "Id пользователя",
            required = true,
            example = "7771",
            schema = @Schema(implementation = Long.class)
    )
    @APIResponse(
            responseCode = "200",
            description = "Пользователь успешно разблокирован",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserResponseWrapper.class),
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
            description = "Некорректный Id. Некорректные входные данные.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserResponseWrapper.class),
                    example = """
                            {
                              "data": null,
                              "message": "UserId must be positive and not null"
                            }
                """
            )
    )
    @APIResponse(
            responseCode = "404",
            description = "Пользователь не найден или не найдена роль 'user' для восстановления",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserResponseWrapper.class),
                    example = """
                            {
                              "data": null,
                              "message": "User not found"
                            }
                """
            )
    )
    @APIResponse(
            responseCode = "408",
            description = "Таймаут запроса",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserResponseWrapper.class),
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
                    schema = @Schema(implementation = UserResponseWrapper.class),
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
                    schema = @Schema(implementation = UserResponseWrapper.class),
                    example = """
                            {
                                "data": null,
                                "message": "SqlServer Exception occurred: Unable to connect to database"
                            }
                    """
            )
    )
    public Uni<Response> unblockUser(@PathParam("id") Long id) {
        return repo.unBlockUser(id).onItem()
                .transform(resp->
                        Response.ok().entity(UserResponseWrapper.successWithNoData()).build()
                );
    }
}
