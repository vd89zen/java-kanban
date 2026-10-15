package ru.yandex.practicum.java.tasktracker.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.java.tasktracker.exceptions.BadRequestException;
import ru.yandex.practicum.java.tasktracker.exceptions.ManagerSaveException;
import ru.yandex.practicum.java.tasktracker.exceptions.NotFoundException;
import ru.yandex.practicum.java.tasktracker.task.Subtask;
import ru.yandex.practicum.java.tasktracker.utils.TextForGson;
import ru.yandex.practicum.java.tasktracker.utils.enums.ResultOfOperation;
import ru.yandex.practicum.java.tasktracker.service.TaskManager;
import java.io.IOException;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {

    public SubtasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        ResultOfOperation resultOfOperation;
        TextForGson textForGson = new TextForGson();
        String requestMethod = httpExchange.getRequestMethod();
        String[] splitPath = httpExchange.getRequestURI().getPath().split("/");
        textForGson.setRequest(requestMethod + " " + httpExchange.getRequestURI());
        try {
            if (requestMethod.equals("DELETE")) {
                if (splitPath.length == 3) {
                    int id;
                    try {
                        id = Integer.valueOf(splitPath[2]);
                    } catch (NumberFormatException numberFormatException) {
                        throw new BadRequestException("The ID number format is incorrect: it must be a positive number.");
                    }

                    taskManager.removeSubtaskByIdNumber(id);
                    sendNoContent(httpExchange);
                } else {
                    throw new BadRequestException("For DELETE path must be '/subtasks/{id}'");
                }
                return;
            }

            if (requestMethod.equals("POST")) {
                List<String> contentTypeValues = httpExchange.getRequestHeaders().get("Content-type");
                if ((contentTypeValues == null) || (contentTypeValues.contains("application/json") == false)) {
                    textForGson.setDetail("'Content-type' must be 'application/json'");
                    sendUnsupportedMediaType(httpExchange, gson.toJson(textForGson));
                    return;
                }

                String requestBody = new String(httpExchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
                if (requestBody.isEmpty()) {
                    throw new BadRequestException("Request Body is empty: there is no data to add");
                }

                Subtask taskFromJson;
                try {
                    taskFromJson = gson.fromJson(requestBody, Subtask.class);
                } catch (Exception exception) {
                    throw new BadRequestException("There is incorrect format JSON data to add: " + exception);
                }

                if (splitPath.length == 2) {
                    textForGson.setTaskType("subtask");
                    boolean isCreate = taskFromJson.getIdNumber() <= 0;

                    if (isCreate) {
                        taskFromJson.setIdNumber(0);
                        resultOfOperation = taskManager.addSubtask(taskFromJson);
                        textForGson.setDone("add");
                    } else {
                        resultOfOperation = taskManager.updateSubtask(taskFromJson);
                        textForGson.setDone("update");
                    }

                    textForGson.setTaskIdNumber(taskFromJson.getIdNumber());

                    if (resultOfOperation == ResultOfOperation.SUCCESS) {
                        textForGson.setRequest(null);
                        if (isCreate) {
                            httpExchange.getResponseHeaders().add("Location", "/subtasks/" + taskFromJson.getIdNumber());
                            sendCreated(httpExchange, gson.toJson(textForGson));
                        } else {
                            sendText(httpExchange, gson.toJson(textForGson));
                        }
                    } else {
                        textForGson.setDone(null);
                        textForGson.setDetail(resultOfOperation.name());
                        switch (resultOfOperation) {
                            case ERROR_INTERSECT_TIME:
                                sendConflict(httpExchange, gson.toJson(textForGson));
                                break;
                            case ERROR_MISMATCH_PARENT_ID:
                            case ERROR_OBJECT_NULL:
                            case ERROR_OBJECT_FIELDS_NULL:
                            case ERROR_ID_LESS_ZERO:
                                sendBadRequest(httpExchange, gson.toJson(textForGson));
                        }
                    }
                } else {
                    throw new BadRequestException("For POST path must be '/subtasks'");
                }
                return;
            }

            if (requestMethod.equals("GET")) {
                if (splitPath.length == 2) {
                    sendText(httpExchange, gson.toJson(taskManager.getAllSubtasks()));
                } else if (splitPath.length == 3) {
                    int id;
                    try {
                        id = Integer.valueOf(splitPath[2]);
                    } catch (NumberFormatException numberFormatException) {
                        throw new BadRequestException("The ID number format is incorrect: it must be a positive number.");
                    }
                    sendText(httpExchange, gson.toJson(taskManager.getSubtaskByIdNumber(id)));
                } else {
                    throw new BadRequestException("For GET path must be '/subtasks' or '/subtasks/{id}'");
                }
                return;
            }
            textForGson.setDetail("Method '" + requestMethod + "' is not allowed. Allowed: GET, POST, DELETE");
            sendMethodNotAllowed(httpExchange, gson.toJson(textForGson), "GET,POST,DELETE");
        } catch (NotFoundException notFoundException) {
            textForGson.setDetail(notFoundException.getMessage());
            sendNotFound(httpExchange, gson.toJson(textForGson));
        } catch (BadRequestException badRequestException) {
            textForGson.setDetail(badRequestException.getMessage());
            sendBadRequest(httpExchange, gson.toJson(textForGson));
        } catch (ManagerSaveException managerSaveException) {
            textForGson.setDetail(managerSaveException.getMessage());
            sendError(httpExchange, gson.toJson(textForGson));
        }
    }
}
