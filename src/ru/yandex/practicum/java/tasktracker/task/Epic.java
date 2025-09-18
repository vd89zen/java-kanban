package ru.yandex.practicum.java.tasktracker.task;

import ru.yandex.practicum.java.tasktracker.utils.AbstractTask;
import ru.yandex.practicum.java.tasktracker.utils.ResultOfOperation;
import ru.yandex.practicum.java.tasktracker.utils.StatusProgress;
import ru.yandex.practicum.java.tasktracker.utils.TypesTasks;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Objects;
import java.util.Comparator;
import java.time.Duration;
import java.time.LocalDateTime;

public class Epic extends AbstractTask {
    private final HashMap<Integer, SubtaskFields> subtasksFields;
    private LocalDateTime endDateTime;

    private class SubtaskFields {
        StatusProgress subtaskStatusProgress;
        Duration subtaskDurationInMinutes;
        LocalDateTime subtaskStartDateTime;
        LocalDateTime subtaskEndDateTime;

        SubtaskFields(StatusProgress subtaskStatusProgress, long subtaskDurationInMinutes,
                      LocalDateTime subtaskStartDateTime, LocalDateTime subtaskEndDateTime) {
            this.subtaskStatusProgress = subtaskStatusProgress;
            this.subtaskDurationInMinutes = Duration.ofMinutes(subtaskDurationInMinutes);
            this.subtaskStartDateTime = subtaskStartDateTime;
            this.subtaskEndDateTime = subtaskEndDateTime;
        }

        private void updateFields(Subtask subtask) {
            subtaskStatusProgress = subtask.getStatusProgress();
            subtaskDurationInMinutes = Duration.ofMinutes(subtask.getDurationInMinutes());
            subtaskStartDateTime = subtask.getStartDateTime().orElse(null);
            subtaskEndDateTime = subtask.getEndDateTime().orElse(null);
        }
    }

    public Epic() {
        super();
        subtasksFields = new HashMap<>();
        type = TypesTasks.EPIC;
        endDateTime = null;
    }

    public Epic(Epic epic) {
        super(epic);
        subtasksFields = new HashMap<>(epic.subtasksFields);
        endDateTime = epic.endDateTime;
    }

    public Epic(String name, String description) {
        super(name, description, StatusProgress.NEW);
        subtasksFields = new HashMap<>();
        type = TypesTasks.EPIC;
        endDateTime = null;
    }

    public Epic(int idNumber, String name, String description) {
        super(idNumber, name, description, StatusProgress.NEW);
        subtasksFields = new HashMap<>();
        type = TypesTasks.EPIC;
        endDateTime = null;
    }

    public ResultOfOperation addSubtask(Subtask subtask) {
        if (subtask == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (subtask.getName().isEmpty() || subtask.getDescription().isEmpty()
                || subtask.getStatusProgress() == null || name == null || description == null || statusProgress == null) {
            return ResultOfOperation.ERROR_OBJECT_FIELDS_NULL;
        } else if (subtask.getIdNumber() < 0 || idNumber < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        } else if (subtasksFields.containsKey(subtask.getIdNumber())) {
            return ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS;
        }

        subtasksFields.put(subtask.getIdNumber(), new SubtaskFields(subtask.getStatusProgress(), subtask.getDurationInMinutes(),
                subtask.getStartDateTime().orElse(null), subtask.getEndDateTime().orElse(null)));
        updateCalculatedFields();
        return ResultOfOperation.SUCCESS;
    }

    public ResultOfOperation updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (subtask.getName().isEmpty() || subtask.getDescription().isEmpty()
                || subtask.getStatusProgress() == null || name == null || description == null || statusProgress == null
                || subtasksFields == null) {
            return ResultOfOperation.ERROR_OBJECT_FIELDS_NULL;
        } else if (subtask.getIdNumber() < 0 || idNumber < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        } if (subtasksFields.containsKey(subtask.getIdNumber()) == false) {
            return ResultOfOperation.ERROR_SUBTASK_NOT_FOUND;
        }

        subtasksFields.get(subtask.getIdNumber()).updateFields(subtask);
        updateCalculatedFields();
        return ResultOfOperation.SUCCESS;
    }

    public ResultOfOperation removeSubtask(int idNumberSubtask) {
        if (idNumberSubtask < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        } else if (subtasksFields.containsKey(idNumberSubtask) == false) {
            return ResultOfOperation.ERROR_SUBTASK_NOT_FOUND;
        }

        subtasksFields.remove(idNumberSubtask);
        updateCalculatedFields();
        return ResultOfOperation.SUCCESS;
    }

    public Optional<ArrayList<Integer>> getSubtasksIdNumber() {
        if (subtasksFields.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new ArrayList<>(subtasksFields.keySet()));
    }

    public void removeAllSubtasks() {
        if (subtasksFields.isEmpty()) {
            return;
        }
        subtasksFields.clear();
        updateCalculatedFields();
    }

    private void updateCalculatedFields() {
        updateStatusProgress();
        updateDurationInMinutes();
        updateStartDateTime();
        updateEndDateTime();
    }

    private void updateStatusProgress() {
        if (subtasksFields.isEmpty()) {
            statusProgress = StatusProgress.NEW;
            return;
        }

        statusProgress = subtasksFields.values().stream()
                .map(fields -> fields.subtaskStatusProgress)
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        list -> list.contains(StatusProgress.IN_PROGRESS)
                                ? StatusProgress.IN_PROGRESS
                                : (new HashSet<>(list).size() == 1
                                    ? list.get(0)
                                    : StatusProgress.IN_PROGRESS)
                ));
    }

    private void updateDurationInMinutes() {
        if (subtasksFields.isEmpty()) {
            durationInMinutes = Duration.ZERO;
            return;
        }

        durationInMinutes = subtasksFields.values().stream()
                .map(fields -> fields.subtaskDurationInMinutes)
                .reduce(Duration.ZERO, Duration::plus);
    }

    private void updateStartDateTime() {
        if (subtasksFields.isEmpty()) {
            startDateTime = null;
            return;
        }

        startDateTime = subtasksFields.values().stream()
                .map(fields -> fields.subtaskStartDateTime)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    private void updateEndDateTime() {
        if (subtasksFields.isEmpty()) {
            endDateTime = null;
            return;
        }

        endDateTime = subtasksFields.values().stream()
                .map(fields -> fields.subtaskEndDateTime)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    @Override
    public Optional<LocalDateTime> getEndDateTime() {
        return Optional.ofNullable(endDateTime);
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

        String subtasksSize;
        if (subtasksFields == null) {
            subtasksSize = "null";
        } else {
            subtasksSize = String.format("%d", subtasksFields.size());
        }

        String startEpic = Objects.toString(startDateTime, "null");
        String endEpic = Objects.toString(endDateTime, "null");;

        return String.format("%nEpic{name='%s', description.length='%s', Status Progress='%s', ID number='%d', " +
                        "number of subtasks='%s', duration in minutes='%s', start DateTime='%s', end DateTime='%s'",
                name, descriptionLength, statusProgressName, idNumber, subtasksSize, durationInMinutes.toMinutes(),
                startEpic, endEpic);
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
                && Objects.equals(subtasksFields, epic.subtasksFields);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(idNumber);
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(description);
        result = 31 * result + Objects.hashCode(statusProgress);
        result = 31 * result + Objects.hashCode(subtasksFields);

        return result;
    }
}
