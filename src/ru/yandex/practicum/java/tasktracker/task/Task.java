package ru.yandex.practicum.java.tasktracker.task;

import ru.yandex.practicum.java.tasktracker.manage.ResultOfOperation;

import java.util.Objects;

public class Task extends AbstractTask {

    public Task() {
        super();
        type = TypesTasks.TASK;
    }

    public Task(Task task) {
        super(task);
    }

    public Task(String name, String description, StatusProgress statusProgress) {
        super(name, description, statusProgress);
        type = TypesTasks.TASK;
    }

    public Task(Integer idNumber, String name, String description, StatusProgress statusProgress) {
        super(idNumber, name, description, statusProgress);
        type = TypesTasks.TASK;
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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Task task = (Task) object;

        return idNumber == task.idNumber
                && Objects.equals(name, task.name)
                && Objects.equals(description, task.description)
                && Objects.equals(statusProgress, task.statusProgress);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(idNumber);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(description);
        result = 31 * result + Objects.hashCode(statusProgress);

        return result;
    }
}
