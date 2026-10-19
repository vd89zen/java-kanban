package ru.yandex.practicum.java.tasktracker.utils.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.java.tasktracker.task.Epic;
import ru.yandex.practicum.java.tasktracker.task.Subtask;
import ru.yandex.practicum.java.tasktracker.task.Task;
import ru.yandex.practicum.java.tasktracker.utils.AbstractTask;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Тесты Gson-адаптеров")
class GsonAdaptersTest {
    private static final LocalDateTime BASE = LocalDateTime.of(2026, 1, 1, 10, 0);

    @Nested
    @DisplayName("LocalDateTimeTypeAdapter")
    class LocalDateTimeAdapterTests {
        private final Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .create();

        @Test
        @DisplayName("Сериализация LocalDateTime в формат 'HH:mm, dd:MM:yyyy'")
        void serialize_Test() {
            assertEquals("\"10:00, 01:01:2026\"", gson.toJson(BASE));
        }

        @Test
        @DisplayName("Десериализация LocalDateTime из строки")
        void deserialize_Test() {
            assertEquals(BASE, gson.fromJson("\"10:00, 01:01:2026\"", LocalDateTime.class));
        }

        @Test
        @DisplayName("Десериализация 'EMPTY' даёт null")
        void deserialize_Empty_ReturnsNull_Test() {
            assertNull(gson.fromJson("\"EMPTY\"", LocalDateTime.class));
        }
    }

    @Nested
    @DisplayName("DurationTypeAdapter")
    class DurationAdapterTests {
        private final Gson gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .create();

        @Test
        @DisplayName("Сериализация Duration в минуты")
        void serialize_Test() {
            assertEquals("90", gson.toJson(Duration.ofMinutes(90)));
        }

        @Test
        @DisplayName("Десериализация минут в Duration")
        void deserialize_Test() {
            assertEquals(Duration.ofMinutes(90), gson.fromJson("90", Duration.class));
        }
    }

    @Nested
    @DisplayName("AbstractTaskDeserializer")
    class AbstractTaskDeserializerTests {
        private final Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .registerTypeAdapter(AbstractTask.class, new AbstractTaskDeserializer())
                .create();

        @Test
        @DisplayName("type=TASK даёт Task")
        void deserialize_Task_Test() {
            //Given
            String json = "{\"type\":\"TASK\",\"name\":\"task\",\"statusProgress\":\"NEW\"}";
            //When
            AbstractTask result = gson.fromJson(json, AbstractTask.class);
            //Then
            assertInstanceOf(Task.class, result);
        }

        @Test
        @DisplayName("type=SUBTASK даёт Subtask с parentEpicIdNumber")
        void deserialize_Subtask_Test() {
            //Given
            String json = "{\"type\":\"SUBTASK\",\"name\":\"sub\",\"statusProgress\":\"NEW\","
                    + "\"parentEpicIdNumber\":5}";
            //When
            AbstractTask result = gson.fromJson(json, AbstractTask.class);
            //Then
            assertInstanceOf(Subtask.class, result);
            assertEquals(5, ((Subtask) result).getParentEpicIdNumber());
        }

        @Test
        @DisplayName("type=EPIC даёт Epic")
        void deserialize_Epic_Test() {
            //Given
            String json = "{\"type\":\"EPIC\",\"name\":\"epic\",\"statusProgress\":\"NEW\"}";
            //When
            AbstractTask result = gson.fromJson(json, AbstractTask.class);
            //Then
            assertInstanceOf(Epic.class, result);
        }

        @Test
        @DisplayName("Отсутствие поля type - JsonParseException")
        void deserialize_NoType_Throws_Test() {
            //Given
            String json = "{\"name\":\"task\",\"statusProgress\":\"NEW\"}";
            //When, Then
            assertThrows(JsonParseException.class, () -> gson.fromJson(json, AbstractTask.class));
        }

        @Test
        @DisplayName("type=null - JsonParseException")
        void deserialize_NullType_Throws_Test() {
            //Given
            String json = "{\"type\":null,\"name\":\"task\"}";
            //When, Then
            assertThrows(JsonParseException.class, () -> gson.fromJson(json, AbstractTask.class));
        }

        @Test
        @DisplayName("Неизвестный type - JsonParseException")
        void deserialize_UnknownType_Throws_Test() {
            //Given
            String json = "{\"type\":\"UNKNOWN\",\"name\":\"x\"}";
            //When, Then
            assertThrows(JsonParseException.class, () -> gson.fromJson(json, AbstractTask.class));
        }
    }
}