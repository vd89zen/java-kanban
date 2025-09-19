package ru.yandex.practicum.java.tasktracker.task;

import ru.yandex.practicum.java.tasktracker.utils.AbstractTask;
import ru.yandex.practicum.java.tasktracker.utils.ResultOfOperation;
import ru.yandex.practicum.java.tasktracker.utils.StatusProgress;
import ru.yandex.practicum.java.tasktracker.utils.TypesTasks;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Subtask extends AbstractTask {
    private int parentEpicIdNumber;

    public Subtask() {
        super();
        this.parentEpicIdNumber = -1;
        type = TypesTasks.SUBTASK;
    }

    public Subtask(Subtask subtask) {
        super(subtask);
        this.parentEpicIdNumber = subtask.parentEpicIdNumber;
    }

    public Subtask(String name, String description, StatusProgress statusProgress, int parentEpicIdNumber) {
        super(name, description, statusProgress);
        if (parentEpicIdNumber < 0) {
            throw new IllegalArgumentException("'parentEpicIdNumber' can't be less zero");
        } else {
            this.parentEpicIdNumber = parentEpicIdNumber;
        }
        type = TypesTasks.SUBTASK;
    }

    public Subtask(String name, String description, StatusProgress statusProgress, int parentEpicIdNumber,
                   LocalDateTime startDateTime) {
        super(name, description, statusProgress, startDateTime);
        if (parentEpicIdNumber < 0) {
            throw new IllegalArgumentException("'parentEpicIdNumber' can't be less zero");
        } else {
            this.parentEpicIdNumber = parentEpicIdNumber;
        }
        type = TypesTasks.SUBTASK;
    }

    public Subtask(String name, String description, StatusProgress statusProgress, int parentEpicIdNumber,
                   long durationInMinutes) {
        super(name, description, statusProgress, durationInMinutes);
        if (parentEpicIdNumber < 0) {
            throw new IllegalArgumentException("'parentEpicIdNumber' can't be less zero");
        } else {
            this.parentEpicIdNumber = parentEpicIdNumber;
        }
        type = TypesTasks.SUBTASK;
    }

    public Subtask(String name, String description, StatusProgress statusProgress, int parentEpicIdNumber,
                   long durationInMinutes, LocalDateTime startDateTime) {
        super(name, description, statusProgress, durationInMinutes, startDateTime);
        if (parentEpicIdNumber < 0) {
            throw new IllegalArgumentException("'parentEpicIdNumber' can't be less zero");
        } else {
            this.parentEpicIdNumber = parentEpicIdNumber;
        }
        type = TypesTasks.SUBTASK;
    }

    public Subtask(int idNumber, String name, String description, StatusProgress statusProgress, int parentEpicIdNumber) {
        super(idNumber, name, description, statusProgress);
        if (parentEpicIdNumber < 0) {
            throw new IllegalArgumentException("'parentEpicIdNumber' can't be less zero");
        } else {
            this.parentEpicIdNumber = parentEpicIdNumber;
        }
        type = TypesTasks.SUBTASK;
    }

    public Subtask(int idNumber, String name, String description, StatusProgress statusProgress,
                   int parentEpicIdNumber, LocalDateTime startDateTime) {
        super(idNumber, name, description, statusProgress, startDateTime);
        if (parentEpicIdNumber < 0) {
            throw new IllegalArgumentException("'parentEpicIdNumber' can't be less zero");
        } else {
            this.parentEpicIdNumber = parentEpicIdNumber;
        }
        type = TypesTasks.SUBTASK;
    }

    public Subtask(int idNumber, String name, String description, StatusProgress statusProgress,
                   int parentEpicIdNumber, long durationInMinutes) {
        super(idNumber, name, description, statusProgress, durationInMinutes);
        if (parentEpicIdNumber < 0) {
            throw new IllegalArgumentException("'parentEpicIdNumber' can't be less zero");
        } else {
            this.parentEpicIdNumber = parentEpicIdNumber;
        }
        type = TypesTasks.SUBTASK;
    }

    public Subtask(int idNumber, String name, String description, StatusProgress statusProgress,
                   int parentEpicIdNumber, long durationInMinutes, LocalDateTime startDateTime) {
        super(idNumber, name, description, statusProgress, durationInMinutes, startDateTime);
        if (parentEpicIdNumber < 0) {
            throw new IllegalArgumentException("'parentEpicIdNumber' can't be less zero");
        } else {
            this.parentEpicIdNumber = parentEpicIdNumber;
        }
        type = TypesTasks.SUBTASK;
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

    public ResultOfOperation setParentEpicIdNumber(int parentEpicIdNumber) {
        if (parentEpicIdNumber < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        }

        this.parentEpicIdNumber = parentEpicIdNumber;
        return ResultOfOperation.SUCCESS;
    }

    public int getParentEpicIdNumber() {
        return parentEpicIdNumber;
    }

    @Override
    public String toString() {
        String descriptionLength;
        if (description == null) {
            descriptionLength = "null";
        } else {
            descriptionLength = String.format("%d", description.length());
        }

        String statusProgressName = Objects.toString(statusProgress.name(), "null");
        String startSubtask = Objects.toString(startDateTime, "null");
        String endSubtask = getEndDateTime().map(LocalDateTime::toString).orElse("null");

        return String.format("%nSubtask{name='%s', description.length='%s', Status Progress='%s', ID number='%d', " +
                        "parent epic ID number='%d', duration in minutes='%s', start DateTime='%s', end DateTime='%s'",
                name, descriptionLength, statusProgressName, idNumber, parentEpicIdNumber, durationInMinutes.toMinutes(),
                startSubtask, endSubtask);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        final Subtask subtask = (Subtask) object;

        return idNumber == subtask.idNumber
                && parentEpicIdNumber == subtask.parentEpicIdNumber
                && Objects.equals(name, subtask.name)
                && Objects.equals(description, subtask.description)
                && Objects.equals(statusProgress, subtask.statusProgress);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(idNumber);
        result = 31 * result + Objects.hashCode(parentEpicIdNumber);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(description);
        result = 31 * result + Objects.hashCode(statusProgress);

        return result;
    }
}
