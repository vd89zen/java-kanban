package ru.yandex.practicum.java.tasktracker.http;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import ru.yandex.practicum.java.tasktracker.service.InMemoryTaskManager;
import ru.yandex.practicum.java.tasktracker.task.*;
import ru.yandex.practicum.java.tasktracker.utils.AbstractTask;
import ru.yandex.practicum.java.tasktracker.utils.TextForGson;
import ru.yandex.practicum.java.tasktracker.utils.enums.StatusProgress;
import ru.yandex.practicum.java.tasktracker.utils.gson.*;
import ru.yandex.practicum.java.tasktracker.utils.interfaces.TaskManager;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@DisplayName("Тесты для HttpTaskServer")
class HttpTaskServerTest {

    TaskManager taskManager;
    HttpTaskServer httpTaskServer;
    Gson gson;

    public HttpTaskServerTest() throws IOException {
        taskManager = new InMemoryTaskManager();
        httpTaskServer = new HttpTaskServer(taskManager);
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .registerTypeAdapter(AbstractTask.class, new AbstractTaskDeserializer())
                .create();
    }

    @BeforeEach
    public void Prepare_For_Test() {
        taskManager.removeAllTasks();
        taskManager.removeAllEpics();
        httpTaskServer.start();
    }

    @AfterEach
    public void serverStop() {
        httpTaskServer.stop();
    }

    @Nested
    @DisplayName("Тесты работы эндпоинтов /tasks.")
    class testForTasks {
        @Test
        @DisplayName("Проверяем эндпоинт 'DELETE /tasks{id}' - удаление Task.")
        void endpointTasks_Delete_Task_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            Optional<Task> expectedTask = Optional.empty();

            Task task = new Task("task", "test task", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0));
            taskManager.addTask(task);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/tasks/" + task.getIdNumber());
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .DELETE()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());

            Optional<Task> actualTask = taskManager.getTaskByIdNumber(task.getIdNumber());

            assertEquals(expectedTask, actualTask, "Задача в менеджере не удалилась");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'POST /tasks' - добавление Task.")
        void endpointTasks_Add_Task_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 201;
            int expectedSizeTasksList = 1;
            String expectedTaskDescription = "test task";
            Task task = new Task("task", expectedTaskDescription, StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0));
            String taskJson = gson.toJson(task);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/tasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());

            Optional<ArrayList<Task>> tasksFromManager = taskManager.getAllTasks();
            String actualTaskDescription = tasksFromManager.get().get(0).getDescription().get();
            int actualSizeTasksList = tasksFromManager.get().size();

            assertTrue(tasksFromManager.isPresent(), "Задачи не возвращаются");
            assertEquals(expectedSizeTasksList, actualSizeTasksList, "Неверное количество задач");
            assertEquals(expectedTaskDescription, actualTaskDescription, "Возвращаемая задача не соответствует заданной");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'POST /tasks' - обновление Task.")
        void endpointTasks_Update_Task_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 201;
            StatusProgress expectedStatusProgress = StatusProgress.IN_PROGRESS;

            Task task = new Task("task", "test task", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0));
            taskManager.addTask(task);
            task.setStatusProgress(StatusProgress.IN_PROGRESS);
            String taskJson = gson.toJson(task);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/tasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());

            Task tasksFromManager = taskManager.getTaskByIdNumber(task.getIdNumber()).get();
            StatusProgress actualStatusProgress = tasksFromManager.getStatusProgress();

            assertEquals(expectedStatusProgress, actualStatusProgress, "Задача в менеджере не обновилась");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /tasks' - получение всех Task.")
        void endpointTasks_Get_All_Tasks_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSizeTasksList = 2;

            Task task1 = new Task("task1", "test task1", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0));
            Task task2 = new Task("task2", "test task2", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0).plusHours(1));
            taskManager.addTask(task1);
            taskManager.addTask(task2);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/tasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<Task>> collectionType = new TypeToken<>(){};
            ArrayList<Task> tasksFromServer = gson.fromJson(response.body(), collectionType);
            int actualSizeTasksList = tasksFromServer.size();

            assertEquals(expectedSizeTasksList, actualSizeTasksList, "Неверное количество задач");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /tasks/{id}' - получение конкретной Task.")
        void endpointTasks_Get_That_Task_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;

            Task task1 = new Task("task1", "test task1", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0));
            Task task2 = new Task("task2", "test task2", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0).plusHours(1));
            taskManager.addTask(task1);
            taskManager.addTask(task2);

            Task expectedTask = task2;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/tasks/" + task2.getIdNumber());
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            Task actualTask = gson.fromJson(response.body(), Task.class);

            assertEquals(expectedTask, actualTask, "Возвращаемая задача не равна ожидаемой");
        }

        //4хх
        @Test
        @DisplayName("Проверяем ответ сервера (404) - эндпоинт 'DELETE /tasks{id}', удаление несуществующей задачи.")
        void endpointTasks_Delete_Task_Not_Found_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 404;
            int idNotExistsTask = 666;
            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/tasks/" + idNotExistsTask);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .DELETE()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());
        }

        @Test
        @DisplayName("Проверяем ответ сервера (406) - эндпоинт 'POST /tasks' добавление Task пересекающейся по времени.")
        void endpointTasks_Add_Task_Has_Time_Intersect_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 406;

            Task task = new Task("task", "task test", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0));
            taskManager.addTask(task);
            Task task406 = new Task("task406", "task406 test", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0));

            String task406Json = gson.toJson(task406);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/tasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(task406Json))
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());
        }

        @Test
        @DisplayName("Проверяем ответ сервера (400) - эндпоинт 'POST /tasks' добавление Task с некорректными JSON данными.")
        void endpointTasks_Add_Task_Incorrect_Json_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 400;

            Task task400 = new Task("task400", "task400 test", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0));
            String task400Json = gson.toJson(task400);
            String task400JsonIncorrect = task400Json.replace("NEW", "something");

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/tasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(task400JsonIncorrect))
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());
        }

        @Test
        @DisplayName("Проверяем ответ сервера (404) - эндпоинт 'GET /tasks', запрос всех Tasks, когда список пуст.")
        void endpointTasks_Get_All_Tasks_List_Empty_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 404;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/tasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());
        }

        @Test
        @DisplayName("Проверяем ответ сервера (404) - эндпоинт 'GET /tasks{id}', запрос несуществующей Task.")
        void endpointTasks_Get_Not_Exists_Task_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 404;
            int idNotExistsTask = 666;
            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/tasks/" + idNotExistsTask);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());
        }
    }

    @Nested
    @DisplayName("Тесты работы эндпоинтов /subtasks.")
    class testForSubtasks {
        @Test
        @DisplayName("Проверяем эндпоинт 'DELETE /subtasks{id}' - удаление Subtask.")
        void endpointSubtasks_Delete_Subtask_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            Optional<Subtask> expectedSubtask = Optional.empty();

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);
            Subtask subtask = new Subtask("subtask", "test subtask", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0));
            taskManager.addSubtask(subtask);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/subtasks/" + subtask.getIdNumber());
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .DELETE()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());

            Optional<Subtask> actualSubtask = taskManager.getSubtaskByIdNumber(subtask.getIdNumber());

            assertEquals(expectedSubtask, actualSubtask, "Задача в менеджере не удалилась");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'POST /subtasks' - добавление Subtask.")
        void endpointSubtasks_Add_Subtask_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 201;
            int expectedSizeSubtasksList = 1;
            String expectedSubtaskDescription = "test subtask";

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);
            Subtask subtask = new Subtask("subtask", "test subtask", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0));

            String subtaskJson = gson.toJson(subtask);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/subtasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());

            Optional<ArrayList<Subtask>> subtasksFromManager = taskManager.getAllSubtasks();
            String actualSubtaskDescription = subtasksFromManager.get().get(0).getDescription().get();
            int actualSizeSubtasksList = subtasksFromManager.get().size();

            assertTrue(subtasksFromManager.isPresent(), "Задачи не возвращаются");
            assertEquals(expectedSizeSubtasksList, actualSizeSubtasksList, "Неверное количество задач");
            assertEquals(expectedSubtaskDescription, actualSubtaskDescription, "Возвращаемая задача не соответствует заданной");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'POST /subtasks' - обновление Subtask.")
        void endpointSubtasks_Update_Subtask_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 201;
            StatusProgress expectedStatusProgress = StatusProgress.IN_PROGRESS;

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);
            Subtask subtask = new Subtask("subtask", "test subtask", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0));
            taskManager.addSubtask(subtask);

            subtask.setStatusProgress(StatusProgress.IN_PROGRESS);
            String subtaskJson = gson.toJson(subtask);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/subtasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());

            Subtask subtasksFromManager = taskManager.getSubtaskByIdNumber(subtask.getIdNumber()).get();
            StatusProgress actualStatusProgress = subtasksFromManager.getStatusProgress();

            assertEquals(expectedStatusProgress, actualStatusProgress, "Задача в менеджере не обновилась");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /subtasks' - получение всех Subtask.")
        void endpointSubtasks_Get_All_Subtasks_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSizeSubtasksList = 2;

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);
            Subtask subtask1 = new Subtask("subtask1", "test subtask1", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0));
            Subtask subtask2 = new Subtask("subtask2", "test subtask2", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0).plusHours(1));
            taskManager.addSubtask(subtask1);
            taskManager.addSubtask(subtask2);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/subtasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<Subtask>> collectionType = new TypeToken<>(){};
            ArrayList<Subtask> subtasksFromServer = gson.fromJson(response.body(), collectionType);
            int actualSizeSubtasksList = subtasksFromServer.size();

            assertEquals(expectedSizeSubtasksList, actualSizeSubtasksList, "Неверное количество задач");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /subtasks/{id}' - получение конкретной Subtask.")
        void endpointSubtasks_Get_That_Subtask_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);
            Subtask subtask1 = new Subtask("subtask1", "test subtask1", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0));
            Subtask subtask2 = new Subtask("subtask2", "test subtask2", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0).plusHours(1));
            taskManager.addSubtask(subtask1);
            taskManager.addSubtask(subtask2);

            Subtask expectedSubtask = subtask2;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/subtasks/" + subtask2.getIdNumber());
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            Subtask actualSubtask = gson.fromJson(response.body(), Subtask.class);

            assertEquals(expectedSubtask, actualSubtask, "Возвращаемая задача не равна ожидаемой");
        }

        //4хх
        @Test
        @DisplayName("Проверяем ответ сервера (404) - эндпоинт 'DELETE /subtasks{id}', удаление несуществующей subtask.")
        void endpointSubtasks_Delete_Subtask_Not_Found_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 404;
            int idNotExistsSubtask = 666;
            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/subtasks/" + idNotExistsSubtask);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .DELETE()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());
        }

        @Test
        @DisplayName("Проверяем ответ сервера (406) - эндпоинт 'POST /subtasks' нельзя добавить Subtask пересекающийся по времени.")
        void endpointSubtasks_Add_Subtask_Has_Time_Intersect_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 406;

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);
            Subtask subtask1 = new Subtask("subtask1", "test subtask1", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0));
            Subtask subtask406 = new Subtask("subtask406", "test subtask406", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0));
            taskManager.addSubtask(subtask1);

            String subtask406Json = gson.toJson(subtask406);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/subtasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(subtask406Json))
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());
        }

        @Test
        @DisplayName("Проверяем ответ сервера (400) - эндпоинт 'POST /subtasks' добавление Subtask с некорректными JSON данными.")
        void endpointSubtasks_Add_Subtask_Incorrect_Json_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 400;

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);
            Subtask subtask400 = new Subtask("subtask400", "test subtask400", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0));
            String subtask400Json = gson.toJson(subtask400);
            String subtask400JsonIncorrect = subtask400Json.replace("10", "ten");

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/subtasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(subtask400JsonIncorrect))
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());
        }

        @Test
        @DisplayName("Проверяем ответ сервера (404) - эндпоинт 'GET /subtasks', запрос всех Subtasks, когда список пуст.")
        void endpointSubtasks_Get_All_Subtasks_List_Empty_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 404;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/subtasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());
        }

        @Test
        @DisplayName("Проверяем ответ сервера (404) - эндпоинт 'GET /subtasks{id}', запрос несуществующей Subtask.")
        void endpointSubtasks_Get_Not_Exists_Subtask_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 404;
            int idNotExistsSubtask = 666;
            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/subtasks/" + idNotExistsSubtask);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());
        }
    }

    @Nested
    @DisplayName("Тесты работы эндпоинтов /epics.")
    class testForEpics {
        @Test
        @DisplayName("Проверяем эндпоинт 'DELETE /epics{id}' - удаление Epic.")
        void endpointEpics_Delete_Epic_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            Optional<Epic> expectedEpic = Optional.empty();

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/epics/" + epic.getIdNumber());
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .DELETE()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());

            Optional<Epic> actualEpic = taskManager.getEpicByIdNumber(epic.getIdNumber());

            assertEquals(expectedEpic, actualEpic, "Задача в менеджере не удалилась");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'POST /epics' - добавление Epic.")
        void endpointEpics_Add_Epic_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 201;
            int expectedSizeEpicsList = 1;
            String expectedEpicDescription = "test epic";

            Epic epic = new Epic("epic", expectedEpicDescription);

            String epicJson = gson.toJson(epic);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/epics");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());

            Optional<ArrayList<Epic>> epicsFromManager = taskManager.getAllEpics();
            String actualEpicDescription = epicsFromManager.get().get(0).getDescription().get();
            int actualSizeEpicsList = epicsFromManager.get().size();

            assertTrue(epicsFromManager.isPresent(), "Задачи не возвращаются");
            assertEquals(expectedSizeEpicsList, actualSizeEpicsList, "Неверное количество задач");
            assertEquals(expectedEpicDescription, actualEpicDescription, "Возвращаемая задача не соответствует заданной");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /epics' - получение всех Epic.")
        void endpointEpics_Get_All_Epics_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSizeEpicsList = 2;

            Epic epic1 = new Epic("epic1", "epic1");
            Epic epic2 = new Epic("epic2", "epic2");
            taskManager.addEpic(epic1);
            taskManager.addEpic(epic2);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/epics");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<Epic>> collectionType = new TypeToken<>(){};
            ArrayList<Epic> epicsFromServer = gson.fromJson(response.body(), collectionType);
            int actualSizeEpicsList = epicsFromServer.size();

            assertEquals(expectedSizeEpicsList, actualSizeEpicsList, "Неверное количество задач");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /epics/{id}' - получение конкретного Epic.")
        void endpointEpics_Get_That_Epic_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);

            Epic expectedEpic = epic;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/epics/" + epic.getIdNumber());
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            Epic actualEpic = gson.fromJson(response.body(), Epic.class);

            assertEquals(expectedEpic, actualEpic, "Возвращаемая задача не равна ожидаемой");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /epics/{id}/subtasks' - получение конкретного Subtask.")
        void endpointEpics_Get_That_Subtask_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSizeSubtasksList = 2;

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);
            Subtask subtask1 = new Subtask("subtask1", "test subtask1", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0));
            Subtask subtask2 = new Subtask("subtask2", "test subtask2", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0).plusHours(1));
            taskManager.addSubtask(subtask1);
            taskManager.addSubtask(subtask2);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/epics/" + epic.getIdNumber() + "/subtasks");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<Subtask>> collectionType = new TypeToken<>(){};
            ArrayList<Subtask> subtasksFromServer = gson.fromJson(response.body(), collectionType);
            int actualSizeSubtasksList = subtasksFromServer.size();

            assertEquals(expectedSizeSubtasksList, actualSizeSubtasksList, "Неверное количество задач");
        }

        //4хх
        @Test
        @DisplayName("Проверяем ответ сервера (404) - эндпоинт 'DELETE /epics{id}', удаление несуществующего epic.")
        void endpointEpics_Delete_Epic_Not_Found_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 404;
            int idNotExistsEpic = 666;
            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/epics/" + idNotExistsEpic);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .DELETE()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'POST /epics' - невозможность обновить Epic.")
        void endpointEpics_Update_Epic_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 400;

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);
            epic.setName("updated epic");

            String epicJson = gson.toJson(epic);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/epics");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());
        }

        @Test
        @DisplayName("Проверяем ответ сервера (400) - эндпоинт 'POST /epics' нельзя добавить Epic с некорректными JSON данными.")
        void endpointEpics_Add_Epic_Incorrect_Json_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 400;

            Epic epic400 = new Epic("epic", "epic");

            String epic400Json = gson.toJson(epic400);
            String epic400JsonIncorrect = epic400Json.replace("NEW", "OLD");

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/epics");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .header("Content-type", "application/json")
                    .uri(url)
                    .POST(HttpRequest.BodyPublishers.ofString(epic400JsonIncorrect))
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());
        }

        @Test
        @DisplayName("Проверяем ответ сервера (404) - эндпоинт 'GET /epics', запрос всех Epics, когда список пуст.")
        void endpointEpics_Get_All_Epics_List_Empty_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 404;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/epics");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());
        }

        @Test
        @DisplayName("Проверяем ответ сервера (404) - эндпоинт 'GET /epics{id}', запрос несуществующего Epic.")
        void endpointEpics_Get_Not_Exists_Epic_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 404;
            int idNotExistsEpic = 666;
            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/epics/" + idNotExistsEpic);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());
        }
    }

    @Nested
    @DisplayName("Тесты работы эндпоинтов /history.")
    class testForHistory {
        @Test
        @DisplayName("Проверяем эндпоинт 'GET /history' - получение истории.")
        void endpointHistory_Get_History_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSizeHistory = 3;

            Task task = new Task("task", "test task", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0));
            taskManager.addTask(task);
            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);
            Subtask subtask = new Subtask("subtask", "test subtask", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0).plusHours(1));
            taskManager.addSubtask(subtask);

            taskManager.getTaskByIdNumber(task.getIdNumber());
            taskManager.getEpicByIdNumber(epic.getIdNumber());
            taskManager.getSubtaskByIdNumber(subtask.getIdNumber());

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/history");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<AbstractTask>> collectionType = new TypeToken<>(){};
            ArrayList<AbstractTask> historyFromServer = gson.fromJson(response.body(), collectionType);
            int actualSizeHistory = historyFromServer.size();
            System.out.println(historyFromServer);

            assertEquals(expectedSizeHistory, actualSizeHistory, "Неверное количество задач");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /history', запрос History, когда список пуст.")
        void endpointHistory_Get_History_When_Empty_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSizeHistory = 0;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/history");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<AbstractTask>> collectionType = new TypeToken<>(){};
            ArrayList<AbstractTask> historyFromServer = gson.fromJson(response.body(), collectionType);
            int actualSizeHistory = historyFromServer.size();
            System.out.println(historyFromServer);

            assertEquals(expectedSizeHistory, actualSizeHistory, "Неверное количество задач");
        }

        //4xx
        @Test
        @DisplayName("Проверяем ответ сервера (400) - эндпоинт '/history', но с методом 'DELETE'.")
        void endpointHistory_But_Delete_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 400;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/history");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .DELETE()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());
        }
    }

    @Nested
    @DisplayName("Тесты работы эндпоинтов /prioritized.")
    class testForPrioritized {
        @Test
        @DisplayName("Проверяем эндпоинт 'GET /prioritized' - получение списка приоритетных задач.")
        void endpointPrioritized_Get_Prioritized_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSizePrioritized = 2;

            Epic epic = new Epic("epic", "epic");
            taskManager.addEpic(epic);

            Subtask subtask1 = new Subtask("subtask1", "test subtask1", StatusProgress.NEW,
                    epic.getIdNumber(),10, LocalDateTime.now().withNano(0).plusHours(1));
            taskManager.addSubtask(subtask1);
            Subtask subtask2 = new Subtask("subtask2", "test subtask2", StatusProgress.NEW,
                    epic.getIdNumber());
            taskManager.addSubtask(subtask2);

            Task task1 = new Task("task1", "test task1", StatusProgress.NEW, 10,
                    LocalDateTime.now().withNano(0));
            taskManager.addTask(task1);
            Task task2 = new Task("task2", "test task2", StatusProgress.NEW);
            taskManager.addTask(task2);

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/prioritized");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<AbstractTask>> collectionType = new TypeToken<>(){};
            ArrayList<AbstractTask> prioritizedFromServer = gson.fromJson(response.body(), collectionType);
            int actualSizePrioritized = prioritizedFromServer.size();
            System.out.println(prioritizedFromServer);

            assertEquals(expectedSizePrioritized, actualSizePrioritized, "Неверное количество задач");
        }

        @Test
        @DisplayName("Проверяем эндпоинт 'GET /prioritized' - получение списка приоритетных задач.")
        void endpointPrioritized_Get_Prioritized_When_Empty_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 200;
            int expectedSizePrioritized = 0;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/prioritized");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .GET()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            assertEquals(expectedStatusCode, response.statusCode());

            TypeToken<ArrayList<AbstractTask>> collectionType = new TypeToken<>(){};
            ArrayList<AbstractTask> prioritizedFromServer = gson.fromJson(response.body(), collectionType);
            int actualSizePrioritized = prioritizedFromServer.size();
            System.out.println(prioritizedFromServer);

            assertEquals(expectedSizePrioritized, actualSizePrioritized, "Неверное количество задач");
        }

        //4xx
        @Test
        @DisplayName("Проверяем ответ сервера (400) - эндпоинт '/prioritized', но с методом 'DELETE'.")
        void endpointPrioritized_But_Delete_Test() throws IOException, InterruptedException {
            //Given
            int expectedStatusCode = 400;

            HttpClient httpClient = HttpClient.newHttpClient();
            URI url = URI.create("http://localhost:8080/prioritized");
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(url)
                    .DELETE()
                    .build();
            //When
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //Then
            System.out.println(gson.fromJson(response.body(), TextForGson.class));
            assertEquals(expectedStatusCode, response.statusCode());
        }
    }
}