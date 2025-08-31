package ru.yandex.practicum.java.tasktracker.task;

import ru.yandex.practicum.java.tasktracker.manage.ResultOfOperation;
import java.util.Objects;

public class Subtask extends AbstractTask {
    private Integer parentEpicIdNumber;

    public static Subtask fromString(String commaSeparatedValues) {
        String[] fields = commaSeparatedValues.split(",");
        final Integer idNumber = Integer.valueOf(fields[1]);
        final String name = fields[2];
        final String description = fields[3];
        final StatusProgress statusProgress;
        switch (fields[4]) {
            case "NEW":
                statusProgress = StatusProgress.NEW;
                break;
            case "IN_PROGRESS":
                statusProgress = StatusProgress.IN_PROGRESS;
                break;
            case "DONE":
                statusProgress = StatusProgress.DONE;
                break;
            default:
                statusProgress = null;
        }
        final Integer parentEpicIdNumber = Integer.valueOf(fields[5]);

        return new Subtask(idNumber, name, description, statusProgress, parentEpicIdNumber);
    }

    public Subtask() {
        super();
        this.parentEpicIdNumber = null;
        type = TypesTasks.SUBTASK;
    }

    public Subtask(Subtask subtask) {
        super(subtask);
        this.parentEpicIdNumber = subtask.parentEpicIdNumber;
    }

    public Subtask(String name, String description, StatusProgress statusProgress, Integer parentEpicIdNumber) {
        super(name, description, statusProgress);
        this.parentEpicIdNumber = Objects.requireNonNull(parentEpicIdNumber, "'parentEpicIdNumber' can't be null");
        type = TypesTasks.SUBTASK;
    }

    public Subtask(Integer idNumber, String name, String description, StatusProgress statusProgress, Integer parentEpicIdNumber) {
        super(idNumber, name, description, statusProgress);
        this.parentEpicIdNumber = Objects.requireNonNull(parentEpicIdNumber, "'parentEpicIdNumber' can't be null");
        type = TypesTasks.SUBTASK;
    }

    public ResultOfOperation setStatusProgress(StatusProgress statusProgress) {
        if (statusProgress == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        }

        this.statusProgress = statusProgress;
        return ResultOfOperation.SUCCESS;
    }

    public ResultOfOperation setParentEpicIdNumber(Integer parentEpicIdNumber) {
        if (parentEpicIdNumber == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (parentEpicIdNumber < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        }

        this.parentEpicIdNumber = parentEpicIdNumber;
        return ResultOfOperation.SUCCESS;
    }

    public Integer getParentEpicIdNumber() {
        return parentEpicIdNumber;
    }

    @Override
    public String toString() {
        final String descriptionLength;
        if (description == null) {
            descriptionLength = "null";
        } else {
            descriptionLength = String.format("%d", description.length());
        }

        final String statusProgressName;
        if (statusProgress == null) {
            statusProgressName = "null";
        } else {
            statusProgressName = statusProgress.name();
        }

        return String.format("%nSubtask{name='%s', description.length='%s', Status Progress='%s', ID number='%d', PARENT_EPIC_ID number='%d'",
                name, descriptionLength, statusProgressName, idNumber, parentEpicIdNumber);
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
