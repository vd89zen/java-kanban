package ru.yandex.practicum.java.tasktracker.utils.enums;

public enum HttpResponseCode {
    OK (200),
    CREATED (201),
    BAD_REQUEST (400),
    NOT_FOUND (404),
    NOT_ACCEPTABLE (406),
    INTERNAL_SERVER_ERROR (500);

    private final int code;
    HttpResponseCode(int code){
        this.code = code;
    }
    public int getCode(){
        return code;
    }
}
