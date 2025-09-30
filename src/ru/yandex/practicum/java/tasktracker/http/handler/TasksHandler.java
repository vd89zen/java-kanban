package ru.yandex.practicum.java.tasktracker.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.java.tasktracker.exceptions.ManagerSaveException;
import ru.yandex.practicum.java.tasktracker.task.Task;
import ru.yandex.practicum.java.tasktracker.utils.enums.ResultOfOperation;
import ru.yandex.practicum.java.tasktracker.utils.interfaces.TaskManager;
import ru.yandex.practicum.java.tasktracker.utils.TextForGson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {

    public TasksHandler(TaskManager taskManager) {
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
                        resultOfOperation = taskManager.removeTaskByIdNumber(id);

                        if (resultOfOperation == ResultOfOperation.SUCCESS) {
                            textForGson.setDone("delete");
                            textForGson.setTaskType("task");
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
                    textForGson.setDetail("For DELETE path must be '/tasks/{id}'");
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

                Task taskFromJson = null;
                try {
                    taskFromJson = gson.fromJson(requestBody, Task.class);
                } catch (Exception exception) {
                    textForGson.setDetail("There is incorrect format JSON data to add: " + exception);
                    sendBadRequest(httpExchange, gson.toJson(textForGson));
                    return;
                }

                if (splitPath.length == 2) {
                    textForGson.setTaskType("task");

                    if (taskFromJson.getIdNumber() == 0) {
                        resultOfOperation = taskManager.addTask(taskFromJson);
                        textForGson.setDone("add");
                    } else {
                        resultOfOperation = taskManager.updateTask(taskFromJson);
                        textForGson.setDone("update");
                    }

                    textForGson.setTaskIdNumber(taskFromJson.getIdNumber());

                    if (resultOfOperation == ResultOfOperation.SUCCESS) {
                        sendCreated(httpExchange, gson.toJson(textForGson));
                    } else {
                        textForGson.setDone(null);
                        textForGson.setRequest(requestMethod + " " + httpExchange.getRequestURI());
                        textForGson.setDetail(resultOfOperation.name());
                        switch (resultOfOperation) {
                            case ERROR_INTERSECT_TIME:
                                sendHasInteractions(httpExchange, gson.toJson(textForGson));
                                break;
                            case ERROR_OBJECT_NOT_FOUND:
                                sendNotFound(httpExchange, gson.toJson(textForGson));
                            case ERROR_OBJECT_NULL:
                            case ERROR_OBJECT_FIELDS_NULL:
                            case ERROR_ID_LESS_ZERO:
                                sendBadRequest(httpExchange, gson.toJson(textForGson));
                        }
                    }
                } else {
                    textForGson.setRequest(requestMethod + " " + httpExchange.getRequestURI());
                    textForGson.setDetail("For POST path must be '/tasks'");
                    sendBadRequest(httpExchange, gson.toJson(textForGson));
                }
                return;
            }

            if (requestMethod.equals("GET")) {
                if (splitPath.length == 2) {
                    Optional<ArrayList<Task>> allTasks = taskManager.getAllTasks();
                    if (allTasks.isEmpty()) {
                        textForGson.setDetail("List is empty");
                        sendNotFound(httpExchange, gson.toJson(textForGson));
                    } else {
                        sendText(httpExchange, gson.toJson(allTasks.get()));
                    }
                } else if (splitPath.length == 3) {
                    try {
                        int id = Integer.valueOf(splitPath[2]);
                        if (id > 0) {
                            Optional<Task> task = taskManager.getTaskByIdNumber(id);
                            if (task.isEmpty()) {
                                textForGson.setTaskType("task");
                                textForGson.setTaskIdNumber(id);
                                textForGson.setDetail("Object not found");
                                sendNotFound(httpExchange, gson.toJson(textForGson));
                            } else {
                                sendText(httpExchange, gson.toJson(task.get()));
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
                    textForGson.setDetail("For GET path must be '/tasks' or '/tasks/{id}'");
                    sendBadRequest(httpExchange, gson.toJson(textForGson));
                }
                return;
            }
        } catch (ManagerSaveException exception) {
            textForGson.setRequest(requestMethod + " " + httpExchange.getRequestURI());
            textForGson.setDetail(exception.getMessage());
            sendError(httpExchange, gson.toJson(textForGson));
        }

        textForGson.setRequest(requestMethod + " " + httpExchange.getRequestURI());
        textForGson.setDetail("Check request and try again. Note: only GET/POST/DELETE are supported");
        sendBadRequest(httpExchange, gson.toJson(textForGson));
    }
}
