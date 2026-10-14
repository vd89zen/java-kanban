package ru.yandex.practicum.java.tasktracker.utils.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm, dd:MM:yyyy");

    @Override
    public void write(final JsonWriter jsonWriter, final LocalDateTime localDateTime) throws IOException {
        if (localDateTime == null) {
            jsonWriter.value("EMPTY");
        } else {
            jsonWriter.value(localDateTime.format(timeFormatter));
        }
    }

    @Override
    public LocalDateTime read(final JsonReader jsonReader) throws IOException {
        String textJson = jsonReader.nextString();
        if (textJson.equals("EMPTY")) {
            return null;
        } else {
            return LocalDateTime.parse(textJson, timeFormatter);
        }
    }

}