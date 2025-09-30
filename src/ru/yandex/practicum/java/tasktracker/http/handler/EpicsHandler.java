package ru.yandex.practicum.java.tasktracker.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.java.tasktracker.exceptions.ManagerSaveException;
import ru.yandex.practicum.java.tasktracker.task.Epic;
import ru.yandex.practicum.java.tasktracker.task.Subtask;
import ru.yandex.practicum.java.tasktracker.utils.TextForGson;
import ru.yandex.practicum.java.tasktracker.utils.enums.ResultOfOperation;
import ru.yandex.practicum.java.tasktracker.utils.interfaces.TaskManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {

    public EpicsHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        ResultOfOperation resultOfOperation = null;
        TextForGson textForGson = new TextForGson();
        String requestMethod = httpExchange.getRequestMethod();
        String[] splitPath = httpExchange.getRequestURI().getPath().split("/");
        try {
            if (requestMethod.equals("DELETE")) {
                if (splitPath.length == 3) {
                    try {
                        int id = Integer.valueOf(splitPath[2]);
                        resultOfOperation = taskManager.removeEpicByIdNumber(id);

                        if (resultOfOperation == ResultOfOperation.SUCCESS) {
                            textForGson.setDone("delete");
                            textForGson.setTaskType("epic");
                            textForGson.setTaskIdNumber(id);
                            sendText(httpExchange, gson.toJson(textForGson));
                        } else {
                            textForGson.setRequest(requestMethod + " " + httpExchange.getRequestURI());
                            textForGson.setDetail(resultOfOperation.name());
                            switch (resultOfOperation) {
                                case ERROR_OBJECT_NOT_FOUND:
                                    sendNotFound(httpExchange, gson.toJson(textForGson));
                                case ERROR_ID_LESS_ZERO:
                                    sendBadRequest(httpExchange, gson.toJson(textForGson));
                            }
                        }
                    } catch (NumberFormatException exception) {
                        textForGson.setRequest(requestMethod + " " + httpExchange.getRequestURI());
                        textForGson.setDetail("The ID number format is incorrect: it must be a positive number.");
                        sendBadRequest(httpExchange, gson.toJson(textForGson));
                    }
                } else {
                    textForGson.setRequest(requestMethod + " " + httpExchange.getRequestURI());
                    textForGson.setDetail("For DELETE path must be '/epics/{id}'");
                    sendBadRequest(httpExchange, gson.toJson(textForGson));
                }
                return;
            }

            if (requestMethod.equals("POST")) {
                List<String> contentTypeValues = httpExchange.getRequestHeaders().get("Content-type");
                if ((contentTypeValues == null) || (contentTypeValues.contains("application/json") == false)) {
                    textForGson.setDetail("'Content-type' must be application/json");
                    sendBadRequest(httpExchange, gson.toJson(textForGson));
                    return;
                }

                String requestBody = new String(httpExchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
                if (requestBody.isEmpty()) {
                    textForGson.setDetail("Request Body is empty: there is no data to add");
                    sendBadRequest(httpExchange, gson.toJson(textForGson));
                    return;
                }

                Epic taskFromJson = null;
                try {
                    taskFromJson = gson.fromJson(requestBody, Epic.class);
                } catch (Exception exception) {
                    textForGson.setDetail("There is incorrect format JSON data to add: " + exception);
                    sendBadRequest(httpExchange, gson.toJson(textForGson));
                    return;
                }

                if (splitPath.length == 2) {
                    textForGson.setTaskType("epic");

                    if (taskFromJson.getIdNumber() == 0) {
                        resultOfOperation = taskManager.addEpic(taskFromJson);
                        textForGson.setDone("add");
                    } else {
                        textForGson.setDetail("Updating the epic is prohibited");
                        sendBadRequest(httpExchange, gson.toJson(textForGson));
                        return;
                    }

                    textForGson.setTaskIdNumber(taskFromJson.getIdNumber());

                    if (resultOfOperation == ResultOfOperation.SUCCESS) {
                        sendCreated(httpExchange, gson.toJson(textForGson));
                    } else {
                        textForGson.setDone(null);
                        textForGson.setRequest(requestMethod + " " + httpExchange.getRequestURI());
                        textForGson.setDetail(resultOfOperation.name());
                        sendBadRequest(httpExchange, gson.toJson(textForGson));
                    }
                } else {
                    textForGson.setRequest(requestMethod + " " + httpExchange.getRequestURI());
                    textForGson.setDetail("For POST path must be '/epics'");
                    sendBadRequest(httpExchange, gson.toJson(textForGson));
                }
                return;
            }

            if (requestMethod.equals("GET")) {
                if (splitPath.length == 2) {
                    Optional<ArrayList<Epic>> allEpics = taskManager.getAllEpics();
                    if (allEpics.isEmpty()) {
                        textForGson.setDetail("List is empty");
                        sendNotFound(httpExchange, gson.toJson(textForGson));
                    } else {
                        sendText(httpExchange, gson.toJson(allEpics.get()));
                    }
                } else if (splitPath.length == 3) {
                    try {
                        int id = Integer.valueOf(splitPath[2]);
                        if (id > 0) {
                            Optional<Epic> epic = taskManager.getEpicByIdNumber(id);
                            if (epic.isEmpty()) {
                                textForGson.setTaskType("epic");
                                textForGson.setTaskIdNumber(id);
                                textForGson.setDetail("Object not found");
                                sendNotFound(httpExchange, gson.toJson(textForGson));
                            } else {
                                sendText(httpExchange, gson.toJson(epic.get()));
                            }
                        } else {
                            textForGson.setRequest(requestMethod + " " + httpExchange.getRequestURI());
                            textForGson.setDetail("Error: incorrect ID number");
                            sendBadRequest(httpExchange, gson.toJson(textForGson));
                        }
                    } catch (NumberFormatException exception) {
                        textForGson.setRequest(requestMethod + " " + httpExchange.getRequestURI());
                        textForGson.setDetail("Invalid ID number format");
                        sendBadRequest(httpExchange, gson.toJson(textForGson));
                    }
                } else if (splitPath.length == 4 && splitPath[3].equals("subtasks")) {
                    try {
                        int id = Integer.valueOf(splitPath[2]);
                        if (id > 0) {
                            Optional<ArrayList<Subtask>> subtasksFromEpic = taskManager.getAllSubtasksFromEpic(id);
                            if (subtasksFromEpic.isEmpty()) {
                                textForGson.setDetail("List is empty");
                                sendNotFound(httpExchange, gson.toJson(textForGson));
                            } else {
                                sendText(httpExchange, gson.toJson(subtasksFromEpic.get()));
                            }
                        } else {
                            textForGson.setRequest(requestMethod + " " + httpExchange.getRequestURI());
                            textForGson.setDetail("Error: incorrect ID number");
                            sendBadRequest(httpExchange, gson.toJson(textForGson));
                        }
                    } catch (NumberFormatException exception) {
                        textForGson.setRequest(requestMethod + " " + httpExchange.getRequestURI());
                        textForGson.setDetail("Invalid ID number format");
                        sendBadRequest(httpExchange, gson.toJson(textForGson));
                    }
                } else {
                    textForGson.setRequest(requestMethod + " " + httpExchange.getRequestURI());
                    textForGson.setDetail("For GET path must be '/epics' or '/epics/{id}' or '/epics/{id}/subtasks'");
                    sendBadRequest(httpExchange, gson.toJson(textForGson));
                }
                return;
            }
        } catch (ManagerSaveException exception) {
            textForGson.setRequest(requestMethod + " " + httpExchange.getRequestURI());
            textForGson.setDetail(exception.getMessage());
            sendError(httpExchange, gson.toJson(textForGson));
            return;
        }

        textForGson.setRequest(requestMethod + " " + httpExchange.getRequestURI());
        textForGson.setDetail("Check request and try again. Note: only GET/POST/DELETE are supported");
        sendBadRequest(httpExchange, gson.toJson(textForGson));
    }
}
