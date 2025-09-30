package ru.yandex.practicum.java.tasktracker.utils;

import java.util.Objects;
import java.util.Optional;

public class TextForGson {
    private String request;
    private String done;
    private Integer taskIdNumber;
    private String taskType;
    private String detail;

    public TextForGson() {
        this.request = null;
        this.done = null;
        this.taskIdNumber = null;
        this.taskType = null;
        this.detail = null;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getRequest() {
        return request;
    }

    public void setDone(String done) {
        this.done = done;
    }

    public String getDone() {
        return done;
    }

    public void setTaskIdNumber(Integer taskIdNumber) {
        this.taskIdNumber = taskIdNumber;
    }

    public Integer getTaskIdNumber() {
        return taskIdNumber;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public String toString() {
        String requestString = Optional.ofNullable(request).orElse("null");
        String doneString = Optional.ofNullable(done).orElse("null");
        String taskIdNumberString = Objects.toString(taskIdNumber, "null");
        String taskTypeString = Optional.ofNullable(taskType).orElse("null");
        String detailString = Optional.ofNullable(detail).orElse("null");

        return String.format("%nTextForGson{request='%s', done='%s', taskIdNumber='%s', taskType='%s', detail='%s'",
                requestString, doneString, taskIdNumberString, taskTypeString, detailString);
    }
}
