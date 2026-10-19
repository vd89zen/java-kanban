package ru.yandex.practicum.java.tasktracker.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.java.tasktracker.exceptions.BadRequestException;
import ru.yandex.practicum.java.tasktracker.exceptions.ManagerSaveException;
import ru.yandex.practicum.java.tasktracker.exceptions.NotFoundException;
import ru.yandex.practicum.java.tasktracker.task.Epic;
import ru.yandex.practicum.java.tasktracker.utils.TextForGson;
import ru.yandex.practicum.java.tasktracker.utils.enums.ResultOfOperation;
import ru.yandex.practicum.java.tasktracker.service.TaskManager;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {

    public EpicsHandler(TaskManager taskManager) {
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
                        id = Integer.parseInt(splitPath[2]);
                    } catch (NumberFormatException numberFormatException) {
                        throw new BadRequestException("The ID number format is incorrect: it must be a positive number.");
                    }

                    taskManager.removeEpicByIdNumber(id);
                    sendNoContent(httpExchange);
                } else {
                    throw new BadRequestException("For DELETE path must be '/epics/{id}'");
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

                Epic taskFromJson;
                try {
                    taskFromJson = gson.fromJson(requestBody, Epic.class);
                } catch (Exception exception) {
                    throw new BadRequestException("There is incorrect format JSON data to add: " + exception);
                }

                if (splitPath.length == 2) {
                    textForGson.setTaskType("epic");

                    if (taskFromJson.getIdNumber() <= 0) {
                        taskFromJson.setIdNumber(0);
                        resultOfOperation = taskManager.addEpic(taskFromJson);
                        textForGson.setDone("add");
                    } else {
                        throw new BadRequestException("Updating the epic is prohibited");
                    }

                    textForGson.setTaskIdNumber(taskFromJson.getIdNumber());

                    if (resultOfOperation == ResultOfOperation.SUCCESS) {
                        textForGson.setRequest(null);
                        httpExchange.getResponseHeaders().add("Location", "/epics/" + taskFromJson.getIdNumber());
                        sendCreated(httpExchange, gson.toJson(textForGson));
                    } else {
                        textForGson.setDone(null);
                        textForGson.setDetail(resultOfOperation.name());
                        sendBadRequest(httpExchange, gson.toJson(textForGson));
                    }
                } else {
                    throw new BadRequestException("For POST path must be '/epics'");
                }
                return;
            }

            if (requestMethod.equals("GET")) {
                if (splitPath.length == 2) {
                    QueryParams params = parseQueryParams(httpExchange);
                    List<Epic> result = taskManager.getAllEpics().stream()
                            .filter(epic -> params.status() == null
                                    || epic.getStatusProgress() == params.status())
                            .sorted(comparatorFor(params.sortBy()))
                            .skip(params.from())
                            .limit(params.size())
                            .collect(Collectors.toList());
                    sendText(httpExchange, gson.toJson(result));
                } else if (splitPath.length == 3) {
                    int id;
                    try {
                        id = Integer.parseInt(splitPath[2]);
                    } catch (NumberFormatException numberFormatException) {
                        throw new BadRequestException("The ID number format is incorrect: it must be a positive number.");
                    }
                    sendText(httpExchange, gson.toJson(taskManager.getEpicByIdNumber(id)));
                } else if (splitPath.length == 4 && splitPath[3].equals("subtasks")) {
                    int id;
                    try {
                        id = Integer.parseInt(splitPath[2]);
                    } catch (NumberFormatException numberFormatException) {
                        throw new BadRequestException("The ID number format is incorrect: it must be a positive number.");
                    }
                    sendText(httpExchange, gson.toJson(taskManager.getAllSubtasksFromEpic(id)));
                } else {
                    throw new BadRequestException("For GET path must be '/epics' or '/epics/{id}' or '/epics/{id}/subtasks'");
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
