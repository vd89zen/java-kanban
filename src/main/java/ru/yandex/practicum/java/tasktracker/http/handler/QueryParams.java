package ru.yandex.practicum.java.tasktracker.http.handler;

import ru.yandex.practicum.java.tasktracker.utils.enums.StatusProgress;

public record QueryParams(int from, int size, StatusProgress status, String sortBy) {

    public static QueryParams defaults() {
        return new QueryParams(0, 20, null, "id");
    }
}