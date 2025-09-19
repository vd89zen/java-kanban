package ru.yandex.practicum.java.tasktracker.task;

import ru.yandex.practicum.java.tasktracker.utils.AbstractTask;
import ru.yandex.practicum.java.tasktracker.utils.ResultOfOperation;
import ru.yandex.practicum.java.tasktracker.utils.StatusProgress;
import ru.yandex.practicum.java.tasktracker.utils.TypesTasks;
import java.time.Duration;
import java.time.LocalDateTime;
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

    public Task(String name, String description, StatusProgress statusProgress, LocalDateTime startDateTime) {
        super(name, description, statusProgress, startDateTime);
        type = TypesTasks.TASK;
    }

    public Task(String name, String description, StatusProgress statusProgress, long durationInMinutes) {
        super(name, description, statusProgress, durationInMinutes);
        type = TypesTasks.TASK;
    }

    public Task(String name, String description, StatusProgress statusProgress, long durationInMinutes,
                LocalDateTime startDateTime) {
        super(name, description, statusProgress, durationInMinutes, startDateTime);
        type = TypesTasks.TASK;
    }

    public Task(int idNumber, String name, String description, StatusProgress statusProgress) {
        super(idNumber, name, description, statusProgress);
        type = TypesTasks.TASK;
    }

    public Task(int idNumber, String name, String description, StatusProgress statusProgress, LocalDateTime startDateTime) {
        super(idNumber, name, description, statusProgress, startDateTime);
        type = TypesTasks.TASK;
    }

    public Task(int idNumber, String name, String description, StatusProgress statusProgress, long durationInMinutes) {
        super(idNumber, name, description, statusProgress, durationInMinutes);
        type = TypesTasks.TASK;
    }

    public Task(int idNumber, String name, String description, StatusProgress statusProgress, long durationInMinutes,
                LocalDateTime startDateTime) {
        super(idNumber, name, description, statusProgress, durationInMinutes, startDateTime);
        type = TypesTasks.TASK;
    }

    public ResultOfOperation setStatusProgress(StatusProgress statusProgress) {
        if (statusProgress == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        }

        this.statusProgress = statusProgress;
        return ResultOfOperation.SUCCESS;
    }

    public ResultOfOperation setDurationInMinutes(long durationInMinutes) {
        if (durationInMinutes < 0) {
            return  ResultOfOperation.ERROR_DURATION_LESS_ZERO;
        } else {
            this.durationInMinutes = Duration.ofMinutes(durationInMinutes);
            return ResultOfOperation.SUCCESS;
        }
    }

    public ResultOfOperation setStartDateTime(LocalDateTime startDateTime) {
        if (startDateTime == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        }
        this.startDateTime = startDateTime;
        return ResultOfOperation.SUCCESS;
    }

    public void resetStartDateTime() {
        this.startDateTime = null;
    }

    @Override
    public String toString() {
        String descriptionLength;
        if (description == null) {
            descriptionLength = "null";
        } else {
            descriptionLength = String.format("%d",description.length());
        }

        String statusProgressName = Objects.toString(statusProgress.name(), "null");
        String startTask = Objects.toString(startDateTime, "null");
        String endTask = getEndDateTime()
                .map(LocalDateTime::toString)
                .orElse("null");

        return String.format("%nTask{name='%s', description.length='%s', Status Progress='%s', ID number='%d', " +
                        "duration in minutes='%s', start DateTime='%s', end DateTime='%s'",
                name, descriptionLength, statusProgressName, idNumber, durationInMinutes.toMinutes(), startTask,
                endTask);
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
