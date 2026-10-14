package ru.yandex.practicum.java.tasktracker.utils.enums;

public enum HttpResponseCode {
    OK(200),
    CREATED(201),
    NO_CONTENT(204),
    BAD_REQUEST(400),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    NOT_ACCEPTABLE(406),
    CONFLICT(409),
    UNSUPPORTED_MEDIA_TYPE(415),
    INTERNAL_SERVER_ERROR(500);

    private final int code;

    HttpResponseCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
