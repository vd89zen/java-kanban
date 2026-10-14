package ru.yandex.practicum.java.tasktracker.utils.gson;

import com.google.gson.*;
import ru.yandex.practicum.java.tasktracker.task.Epic;
import ru.yandex.practicum.java.tasktracker.task.Subtask;
import ru.yandex.practicum.java.tasktracker.task.Task;
import ru.yandex.practicum.java.tasktracker.utils.AbstractTask;
import java.lang.reflect.Type;

public class AbstractTaskDeserializer implements JsonDeserializer<AbstractTask> {
    @Override
    public AbstractTask deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("type").getAsString();

        switch (type) {
            case "TASK":
                return context.deserialize(jsonObject, Task.class);
            case "SUBTASK":
                return context.deserialize(jsonObject, Subtask.class);
            case "EPIC":
                return context.deserialize(jsonObject, Epic.class);
            default:
                throw new JsonParseException("Неизвестный тип задачи: " + type);
        }
    }
}
