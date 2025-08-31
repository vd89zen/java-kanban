package ru.yandex.practicum.java.tasktracker.task;

import ru.yandex.practicum.java.tasktracker.manage.ResultOfOperation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Epic extends AbstractTask {
    private final HashMap<Integer, StatusProgress> subtasks;

    public Epic() {
        super();
        subtasks = null;
        type = TypesTasks.EPIC;
    }

    public Epic(Epic epic) {
        super(epic);
        subtasks = epic.subtasks;
    }

    public Epic(String name, String description) {
        super(name, description, StatusProgress.NEW);
        subtasks = new HashMap<>();
        type = TypesTasks.EPIC;
    }

    public Epic(Integer idNumber, String name, String description) {
        super(idNumber, name, description, StatusProgress.NEW);
        subtasks = new HashMap<>();
        type = TypesTasks.EPIC;
    }

    public ResultOfOperation addSubtask(Integer subtaskIdNumber, StatusProgress subtaskStatusProgress) {
        if (subtaskIdNumber == null || subtaskStatusProgress == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        }

        if (subtasks.containsKey(subtaskIdNumber) == false) {
            subtasks.put(subtaskIdNumber, subtaskStatusProgress);
            updateStatusProgress();
            return ResultOfOperation.SUCCESS;
        } else {
            return ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS;
        }
    }

    public ResultOfOperation removeSubtask(Integer subtaskIdNumber) {
        if (subtaskIdNumber == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        }

        if (subtasks.containsKey(subtaskIdNumber)) {
            subtasks.remove(subtaskIdNumber);
            updateStatusProgress();
            return ResultOfOperation.SUCCESS;
        } else {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        }
    }

    public ArrayList<Integer> getSubtasksIdNumber() {
        if (subtasks.isEmpty()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(subtasks.keySet());
    }

    public HashMap<Integer, StatusProgress> getSubtasksIdAndStatus() {
        if (subtasks.isEmpty()) {
            return new HashMap<>();
        }

        return new HashMap<>(subtasks);
    }

    public void removeAllSubtasksIdNumber() {
        subtasks.clear();
        updateStatusProgress();
    }

    public ResultOfOperation updateSubtask(Integer subtaskIdNumber, StatusProgress subtaskStatusProgress) {
        if (subtaskIdNumber == null || subtaskStatusProgress == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        }

        if (subtasks.replace(subtaskIdNumber, subtaskStatusProgress) == null) {
            return ResultOfOperation.ERROR_SUBTASK_NOT_FOUND;
        } else {
            updateStatusProgress();
            return ResultOfOperation.SUCCESS;
        }
    }

    private void updateStatusProgress() {
        if (subtasks.isEmpty()) {
            statusProgress = StatusProgress.NEW;
            return;
        }

        boolean epicDONE = true;
        boolean epicNEW = true;

        for (StatusProgress subtaskStatusProgress : subtasks.values()) {
            if (epicDONE) {
                if (subtaskStatusProgress == StatusProgress.IN_PROGRESS) {
                    statusProgress = StatusProgress.IN_PROGRESS;
                    return;
                } else if (subtaskStatusProgress == StatusProgress.NEW) {
                    epicDONE = false;
                }
            }

            if (epicNEW) {
                if (subtaskStatusProgress == StatusProgress.IN_PROGRESS) {
                    statusProgress = StatusProgress.IN_PROGRESS;
                    return;
                } else if (subtaskStatusProgress == StatusProgress.DONE) {
                    epicNEW = false;
                }
            }
        }

        if (epicDONE) {
            statusProgress = StatusProgress.DONE;
        } else if (epicNEW) {
            statusProgress = StatusProgress.NEW;
        } else {
            statusProgress = StatusProgress.IN_PROGRESS;
        }
    }

    @Override
    public String toString() {
        String descriptionLength;
        if (description == null) {
            descriptionLength = "null";
        } else {
            descriptionLength = String.format("%d", description.length());
        }

        String statusProgressName;
        if (statusProgress == null) {
            statusProgressName = "null";
        } else {
            statusProgressName = statusProgress.name();
        }

        String subtasksSize;
        if (subtasks == null) {
            subtasksSize = "null";
        } else {
            subtasksSize = String.format("%d", subtasks.size());
        }

        return String.format("%nEpic{name='%s', description.length='%s', Status Progress='%s', ID number='%d', number of subtusks='%s'",
                name, descriptionLength, statusProgressName, idNumber, subtasksSize);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Epic epic = (Epic) object;

        return idNumber == epic.idNumber
                && Objects.equals(name, epic.name)
                && Objects.equals(description, epic.description)
                && Objects.equals(statusProgress, epic.statusProgress)
                && Objects.equals(subtasks, epic.subtasks);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(idNumber);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(description);
        result = 31 * result + Objects.hashCode(statusProgress);
        result = 31 * result + Objects.hashCode(subtasks);

        return result;
    }
}
