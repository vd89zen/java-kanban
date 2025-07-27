package ru.yandex.practicum.java.tasktracker.task;

import ru.yandex.practicum.java.tasktracker.manage.ResultOfOperation;

public class Task extends AbstractTask {

    public Task() {
        super();
    }

    public Task(Task task) {
        super(task);
    }

    public Task(String name, String description, StatusProgress statusProgress) {
        super(name, description, statusProgress);
    }

    public Task(String name, String description, StatusProgress statusProgress, Integer idNumber) {
        super(name, description, statusProgress, idNumber);
    }

    public ResultOfOperation setStatusProgress(StatusProgress statusProgress) {
        if (statusProgress == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        }

        this.statusProgress = statusProgress;
        return ResultOfOperation.SUCCESS;
    }

    @Override
    public String toString() {
        String descriptionLength;
        if (description == null) {
            descriptionLength = "null";
        } else {
            descriptionLength = String.format("%d",description.length());
        }

        String statusProgressName;
        if (statusProgress == null) {
            statusProgressName = "null";
        } else {
            statusProgressName = statusProgress.name();
        }

        return String.format("%nTask{name='%s', description.length='%s', Status Progress='%s', ID number='%d'",
                name, descriptionLength, statusProgressName, idNumber);
    }
}
