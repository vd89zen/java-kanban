package ru.yandex.practicum.java.tasktracker.utils;

import ru.yandex.practicum.java.tasktracker.task.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommaSeparatedValuesCreateProcess {
    private int idNumber;
    private String name;
    private String description;
    private StatusProgress statusProgress;
    private int parentEpicIdNumber;
    private long durationInMinutes;
    private LocalDateTime startDateTime;
    private final String headersForCsv;
    private final byte headerId;
    private final byte headerType;
    private final byte headerName;
    private final byte headerDescription;
    private final byte headerStatusProgress;
    private final byte headerParentEpic;
    private final byte headerChildSubtasks;
    private final byte headerDurationInMinutes;
    private final byte headerStartDateTime;
    private final byte headerEndDateTime;

    public CommaSeparatedValuesCreateProcess() {
        idNumber = -1;
        name = null;
        description = null;
        statusProgress = null;
        parentEpicIdNumber = -1;
        durationInMinutes = 0;
        startDateTime = null;
        headersForCsv = "id,type,name,description,statusProgress,parentEpic,childSubtasks,durationInMinutes,startDateTime,endDateTime";
        headerId = 0;
        headerType = 1;
        headerName = 2;
        headerDescription = 3;
        headerStatusProgress = 4;
        headerParentEpic = 5;
        headerChildSubtasks = 6;
        headerDurationInMinutes = 7;
        headerStartDateTime = 8;
        headerEndDateTime = 9;
    }

    public String toString(Task task) {
        String id = String.valueOf(task.getIdNumber());
        String type = TypesTasks.TASK.name();

        String statusProgress = task.getStatusProgress().name();
        String parentEpic = "SKIP";
        String childSubtasks = "SKIP";
        String durationInMinutes = String.valueOf(task.getDurationInMinutes());

        Optional<LocalDateTime> startTask = task.getStartDateTime();
        String startDateTime;
        if (startTask.isPresent()) {
            startDateTime = startTask.get().toString();
        } else {
            startDateTime = "null";
        }

        Optional<LocalDateTime> endTask = task.getEndDateTime();
        String endDateTime;
        if (endTask.isPresent()) {
            endDateTime = endTask.get().toString();
        } else {
            endDateTime = "null";
        }

        return String.join(",", id, type, task.getName().get(), task.getDescription().get(),
                statusProgress, parentEpic, childSubtasks, durationInMinutes, startDateTime, endDateTime);
    }

    public String toString(Subtask subtask) {
        String id = String.valueOf(subtask.getIdNumber());
        String type = TypesTasks.SUBTASK.name();

        String statusProgress = subtask.getStatusProgress().name();
        String parentEpic = String.valueOf(subtask.getParentEpicIdNumber());
        String childSubtasks = "SKIP";
        String durationInMinutes = String.valueOf(subtask.getDurationInMinutes());

        Optional<LocalDateTime> startSubtask = subtask.getStartDateTime();
        String startDateTime;
        if (startSubtask.isPresent()) {
            startDateTime = startSubtask.get().toString();
        } else {
            startDateTime = "null";
        }

        Optional<LocalDateTime> endSubtask = subtask.getEndDateTime();
        String endDateTime;
        if (endSubtask.isPresent()) {
            endDateTime = endSubtask.get().toString();
        } else {
            endDateTime = "null";
        }

        return String.join(",", id, type, subtask.getName().get(), subtask.getDescription().get(),
                statusProgress, parentEpic, childSubtasks, durationInMinutes, startDateTime, endDateTime);
    }

    public String toString(Epic epic) {
        String id = String.valueOf(epic.getIdNumber());
        String type = TypesTasks.EPIC.name();

        String statusProgress = epic.getStatusProgress().name();
        String parentEpic = "SKIP";

        String childSubtasks;
        Optional<ArrayList<Integer>> subtasks = epic.getSubtasksIdNumber();
        if (subtasks.isEmpty()) {
            childSubtasks = "EMPTY";
        } else {
            childSubtasks = subtasks.get().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining("|"));
        }

        String durationInMinutes = String.valueOf(epic.getDurationInMinutes());

        Optional<LocalDateTime> startEpic = epic.getStartDateTime();
        String startDateTime;
        if (startEpic.isPresent()) {
            startDateTime = startEpic.get().toString();
        } else {
            startDateTime = "null";
        }

        Optional<LocalDateTime> endEpic = epic.getEndDateTime();
        String endDateTime;
        if (endEpic.isPresent()) {
            endDateTime = endEpic.get().toString();
        } else {
            endDateTime = "null";
        }

        return String.join(",", id, type, epic.getName().get(), epic.getDescription().get(), statusProgress,
                parentEpic, childSubtasks, durationInMinutes, startDateTime, endDateTime);
    }

    public Task getTaskFromStrings(String[] strings) {
        setFieldsFromStrings(strings);
        Task task;
        if (startDateTime == null) {
            task = new Task(idNumber, name, description, statusProgress, durationInMinutes);
        } else {
            task = new Task(idNumber, name, description, statusProgress, durationInMinutes, startDateTime);
        }
        resetFields();
        return task;
    }

    public Subtask getSubtaskFromStrings(String[] strings) {
        setFieldsFromStrings(strings);
        Subtask subtask;
        if (startDateTime == null) {
            subtask = new Subtask(idNumber, name, description, statusProgress, parentEpicIdNumber,
                    durationInMinutes);
        } else {
            subtask = new Subtask(idNumber, name, description, statusProgress, parentEpicIdNumber,
                    durationInMinutes, startDateTime);
        }
        resetFields();
        return subtask;
    }

    public Epic getEpicFromStrings(String[] strings) {
        setFieldsFromStrings(strings);
        Epic epic = new Epic(idNumber, name, description);
        resetFields();
        return epic;
    }

    public String getHeadersForCSV() {
        return headersForCsv;
    }

    private void resetFields() {
        idNumber = -1;
        name = null;
        description = null;
        statusProgress = null;
        parentEpicIdNumber = -1;
    }

    private void setFieldsFromStrings(String[] fields) {
        idNumber = Integer.valueOf(fields[headerId]);
        name = fields[headerName];
        description = fields[headerDescription];
        switch (fields[headerStatusProgress]) {
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

        switch (fields[headerParentEpic]) {
            case "SKIP":
                break;
            default:
                parentEpicIdNumber = Integer.valueOf(fields[headerParentEpic]);
        }

        durationInMinutes = Long.valueOf(fields[headerDurationInMinutes]);
        if (fields[headerStartDateTime].equals("null")) {
            startDateTime = null;
        } else {
            startDateTime = LocalDateTime.parse(fields[headerStartDateTime]);
        }
    }

}
