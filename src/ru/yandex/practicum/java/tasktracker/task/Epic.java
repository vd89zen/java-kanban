package ru.yandex.practicum.java.tasktracker.task;

import ru.yandex.practicum.java.tasktracker.manage.StatusProgress;
import java.util.HashMap;
import java.util.Objects;

//добавил проверки для доступа вне менедржера
public class Epic extends Task {
    private final HashMap<Integer, Subtask> subtasks;
    private StatusProgress statusProgress;

    public Epic(String name, String description) {
        super(name, description);
        subtasks = new HashMap<>();
        updateStatusProgress();
    }

    public void addSubtask(Subtask subtask) {
        Objects.requireNonNull(subtask, "'subtask' can't be null");
        subtasks.put(subtask.getIdNumber(), subtask);
        updateStatusProgress();
    }

    //метод идентичен addSubtask - но если надо будет изменить логику обновления, так будет проще
    public void updateSubtask(Subtask subtask) {
        Objects.requireNonNull(subtask, "'subtask' can't be null");
        subtasks.put(subtask.getIdNumber(), subtask);
        updateStatusProgress();
    }

    public Subtask getSubtaskForIdNumber(Integer subtaskIdNumber) {
        Objects.requireNonNull(subtaskIdNumber, "'subtaskIdNumber' can't be null");
        return Objects.requireNonNull(subtasks.get(subtaskIdNumber), "subtask not finded");
    }

    public void removeSubtaskForIdNumber(Integer subtaskIdNumber) {
        Objects.requireNonNull(subtaskIdNumber, "'subtaskIdNumber' can't be null");
        subtasks.remove(subtaskIdNumber);
        updateStatusProgress();
    }

    public void removeAllSubtasks() {
        subtasks.clear();
        updateStatusProgress();
    }

    public HashMap<Integer, Subtask> getAllSubtasks() {
        return subtasks;
    }

    private void updateStatusProgress() {
        //for buildObjectNull in TaskManager
        if (getName() == "null") {
            statusProgress = StatusProgress.NULL;
            return;
        }

        if (subtasks.isEmpty()) {
            statusProgress = StatusProgress.NEW;
            return;
        }

        boolean epicDONE = true;
        boolean epicNEW = true;

        for (Subtask subtask : subtasks.values()) {
            StatusProgress statusSubtask = subtask.statusProgress;
            if (epicDONE) {
                if (statusSubtask == StatusProgress.IN_PROGRESS) {
                    statusProgress = StatusProgress.IN_PROGRESS;
                    return;
                } else if (statusSubtask == StatusProgress.NEW) {
                    epicDONE = false;
                }
            }

            if (epicNEW) {
                if (statusSubtask == StatusProgress.IN_PROGRESS) {
                    statusProgress = StatusProgress.IN_PROGRESS;
                    return;
                } else if (statusSubtask == StatusProgress.DONE) {
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
    public StatusProgress getStatusProgress() {
        return statusProgress;
    }

    @Override
    public String toString() {
        String descriptionLength;
        if (getDescription() == null) {
            descriptionLength = "null";
        } else {
            descriptionLength = String.format("%d",getDescription().length());
        }

        return String.format("%nEpic{name='%s', description.length='%s', Status Progress='%s', ID number='%d', number of subtusks='%d'",
                getName(), descriptionLength, statusProgress.name(), getIdNumber(), subtasks.size());
    }
}
