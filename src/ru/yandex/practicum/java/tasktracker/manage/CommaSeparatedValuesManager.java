package ru.yandex.practicum.java.tasktracker.manage;

import ru.yandex.practicum.java.tasktracker.task.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class CommaSeparatedValuesManager {
    private Integer idNumber;
    private String name;
    private String description;
    private StatusProgress statusProgress;
    private Integer parentEpicIdNumber;
    private HashMap<Integer, StatusProgress> epicSubtasks;
    private final String headersForCsv;
    private final byte headerId;
    private final byte headerType;
    private final byte headerName;
    private final byte headerDescription;
    private final byte headerStatusProgress;
    private final byte headerParentEpic;
    private final byte headerChildSubtasks;

    public CommaSeparatedValuesManager() {
        idNumber = null;
        name = null;
        description = null;
        statusProgress = null;
        parentEpicIdNumber = null;
        epicSubtasks = null;
        headersForCsv = "id,type,name,description,statusProgress,parentEpic,childSubtasks";
        headerId = 0;
        headerType = 1;
        headerName = 2;
        headerDescription = 3;
        headerStatusProgress = 4;
        headerParentEpic = 5;
        headerChildSubtasks = 6;
    }

    public String toString(Task task) {
        final String idNumber;
        if (task.getIdNumber() == null) {
            idNumber = "null";
        } else {
            idNumber = task.getIdNumber().toString();
        }

        final String type = TypesTasks.TASK.name();

        String statusProgress;
        if (task.getStatusProgress() == null) {
            statusProgress = "null";
        } else {
            statusProgress = task.getStatusProgress().name();
        }

        final String parentEpicIdNumber = "SKIP";
        final String subtasksIdAndStatus = "SKIP";

        return String.join(",", idNumber, type, task.getName(),
                task.getDescription(), statusProgress, parentEpicIdNumber, subtasksIdAndStatus);
    }

    public String toString(Subtask subtask) {
        final String idNumber;
        if (subtask.getIdNumber() == null) {
            idNumber = "null";
        } else {
            idNumber = subtask.getIdNumber().toString();
        }

        final String type = TypesTasks.SUBTASK.name();

        final String statusProgress;
        if (subtask.getStatusProgress() == null) {
            statusProgress = "null";
        } else {
            statusProgress = subtask.getStatusProgress().name();
        }

        final String parentEpicIdNumber;
        if (subtask.getParentEpicIdNumber() == null) {
            parentEpicIdNumber = "null";
        } else {
            parentEpicIdNumber = subtask.getParentEpicIdNumber().toString();
        }

        final String subtasksIdAndStatus = "SKIP";

        return String.join(",", idNumber, type, subtask.getName(),
                subtask.getDescription(), statusProgress, parentEpicIdNumber, subtasksIdAndStatus);
    }

    public String toString(Epic epic) {
        final String idNumber;
        if (epic.getIdNumber() == null) {
            idNumber = "null";
        } else {
            idNumber = epic.getIdNumber().toString();
        }

        final String type = TypesTasks.EPIC.name();

        final String statusProgress;
        if (epic.getStatusProgress() == null) {
            statusProgress = "null";
        } else {
            statusProgress = epic.getStatusProgress().name();
        }

        final String parentEpicIdNumber = "SKIP";

        final String subtasksIdAndStatus;
        final HashMap<Integer, StatusProgress> subtasks = new HashMap<>(epic.getSubtasksIdAndStatus());
        if (subtasks.isEmpty() || subtasks == null) {
            subtasksIdAndStatus = "null";
        } else {
            final ArrayList<String> subtasksList = new ArrayList<>(subtasks.size());
            for (Entry<Integer, StatusProgress> entry : subtasks.entrySet()) {
                subtasksList.add(entry.getKey().toString());
                subtasksList.add(entry.getValue().name());
            }
            subtasksIdAndStatus = String.join("-", subtasksList);
        }

        return String.join(",", idNumber, type, epic.getName(),
                epic.getDescription(), statusProgress, parentEpicIdNumber, subtasksIdAndStatus);
    }

    public Task getTaskFromStrings(String[] strings) {
        setFieldsFromStrings(strings);
        Task task = new Task(idNumber, name, description, statusProgress);
        resetFields();
        return task;
    }

    public Subtask getSubtaskFromStrings(String[] strings) {
        setFieldsFromStrings(strings);
        Subtask subtask = new Subtask(idNumber, name, description, statusProgress, parentEpicIdNumber);
        resetFields();
        return subtask;
    }

    public Epic getEpicFromStrings(String[] strings) {
        setFieldsFromStrings(strings);
        Epic epic = new Epic(idNumber, name, description);

        if (epicSubtasks != null) {
            for (Entry<Integer, StatusProgress> entry : epicSubtasks.entrySet()) {
                epic.addSubtask(entry.getKey(), entry.getValue());
            }
        }

        resetFields();
        return epic;
    }

    public String getHeadersForCSV() {
        return headersForCsv;
    }

    private void resetFields() {
        idNumber = null;
        name = null;
        description = null;
        statusProgress = null;
        parentEpicIdNumber = null;
        epicSubtasks = null;
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

        final boolean isSkip = fields[headerChildSubtasks].equals("SKIP") || fields[headerChildSubtasks].equals("null");

        if (isSkip == false) {
            epicSubtasks = new HashMap<>();
            final String[] childSubtasks = fields[headerChildSubtasks].split("-");
            StatusProgress statusProgressSubtask;
            Integer idNumberSubtask;
            for (int i = 0; i < childSubtasks.length; i += 2) {
                idNumberSubtask = Integer.valueOf(childSubtasks[i]);
                switch (childSubtasks[i + 1]) {
                    case "NEW":
                        statusProgressSubtask = StatusProgress.NEW;
                        break;
                    case "IN_PROGRESS":
                        statusProgressSubtask = StatusProgress.IN_PROGRESS;
                        break;
                    case "DONE":
                        statusProgressSubtask = StatusProgress.DONE;
                        break;
                    default:
                        statusProgressSubtask = null;
                }
                epicSubtasks.put(idNumberSubtask, statusProgressSubtask);
            }
        }
    }

}
