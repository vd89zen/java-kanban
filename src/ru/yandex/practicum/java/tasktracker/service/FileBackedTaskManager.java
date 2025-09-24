package ru.yandex.practicum.java.tasktracker.service;

import ru.yandex.practicum.java.tasktracker.exceptions.ManagerSaveException;
import ru.yandex.practicum.java.tasktracker.task.Epic;
import ru.yandex.practicum.java.tasktracker.task.Subtask;
import ru.yandex.practicum.java.tasktracker.task.Task;
import ru.yandex.practicum.java.tasktracker.utils.CommaSeparatedValuesCreateProcess;
import ru.yandex.practicum.java.tasktracker.utils.ResultOfOperation;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.io.File;
import java.io.BufferedWriter;
import java.io.IOException;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private File file;
    private final CommaSeparatedValuesCreateProcess csvManager;
    private static boolean isLoadFromFile;

    public static FileBackedTaskManager loadFromFileUsingMethods(File file) {
        final int HEADER_ID = 0;
        final int HEADER_TYPE = 1;
        if (Files.exists(file.toPath())) {
            FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);
            isLoadFromFile =true;
            try {
                List<String> stringsFromFile = Files.readAllLines(file.toPath());
                ArrayList<Subtask> subtasksList = new ArrayList<>();
                ResultOfOperation resultOfOperation = null;
                for (String string : stringsFromFile) {
                    String[] strings = string.split(",");
                    switch (strings[HEADER_TYPE]) {
                        case "TASK":
                            resultOfOperation = fileBackedTaskManager
                                    .addTask(fileBackedTaskManager.csvManager.getTaskFromStrings(strings));
                            if (resultOfOperation != ResultOfOperation.SUCCESS) {
                                isLoadFromFile = false;
                                throw new RuntimeException("Ошибка добавления TASK (id:" + strings[HEADER_ID] +
                                        ") в базу менеджера: " + resultOfOperation);
                            }
                            break;
                        case "EPIC":
                            resultOfOperation = fileBackedTaskManager
                                    .addEpic(fileBackedTaskManager.csvManager.getEpicFromStrings(strings));
                            if (resultOfOperation != ResultOfOperation.SUCCESS) {
                                isLoadFromFile = false;
                                throw new RuntimeException("Ошибка добавления EPIC (id:" + strings[HEADER_ID] +
                                        ") в базу менеджера: " + resultOfOperation);
                            }
                            break;
                        case "SUBTASK":
                            subtasksList.add(fileBackedTaskManager.csvManager.getSubtaskFromStrings(strings));
                            break;
                        case "type":
                            break;
                        default:
                            isLoadFromFile = false;
                            throw new IOException("Неверный порядок данных в файле");
                    }
                }
                for (Subtask subtask : subtasksList) {
                    resultOfOperation = fileBackedTaskManager.addSubtask(subtask);
                    if (resultOfOperation != ResultOfOperation.SUCCESS) {
                        isLoadFromFile = false;
                        throw new RuntimeException("Ошибка добавления SUBTASK (id:" + subtask.getIdNumber() +
                                ") в базу менеджера: " + resultOfOperation);
                    }
                }
            } catch (IOException exception) {
                isLoadFromFile = false;
                throw new RuntimeException(exception);
            }
            isLoadFromFile = false;
            return fileBackedTaskManager;
        } else {
            return new FileBackedTaskManager(file);
        }
    }

    public static FileBackedTaskManager loadFromFileDirectlyToMap(File file) {
        final int HEADER_TYPE = 1;
        if (Files.exists(file.toPath())) {
            FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file);
            try {
                final List<String> stringsFromFile = Files.readAllLines(file.toPath());
                for (String string : stringsFromFile) {
                    final String[] strings = string.split(",");
                    switch (strings[HEADER_TYPE]) {
                        case "TASK":
                            Task task = fileBackedTaskManager.csvManager.getTaskFromStrings(strings);
                            fileBackedTaskManager.tasks.put(task.getIdNumber(), task);
                            fileBackedTaskManager.allIdInWork.add(task.getIdNumber());
                            break;
                        case "EPIC":
                            Epic epic = fileBackedTaskManager.csvManager.getEpicFromStrings(strings);
                            fileBackedTaskManager.epics.put(epic.getIdNumber(), epic);
                            fileBackedTaskManager.allIdInWork.add(epic.getIdNumber());
                            break;
                        case "SUBTASK":
                            Subtask subtask = fileBackedTaskManager.csvManager.getSubtaskFromStrings(strings);
                            fileBackedTaskManager.subtasks.put(subtask.getIdNumber(), subtask);
                            fileBackedTaskManager.allIdInWork.add(subtask.getIdNumber());
                            break;
                        case "type":
                            break;
                        default:
                            throw new IOException("Неверный порядок данных в файле");
                    }
                }

                for (Subtask subtask : fileBackedTaskManager.subtasks.values()) {
                    Epic parentEpic = fileBackedTaskManager.epics.get(subtask.getParentEpicIdNumber());
                    parentEpic.addSubtask(subtask);
                }
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
            return fileBackedTaskManager;
        } else {
            return new FileBackedTaskManager(file);
        }
    }

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
        csvManager = new CommaSeparatedValuesCreateProcess();
        isLoadFromFile = false;
    }

    private void save() {
        if (isLoadFromFile) {
            return;
        }

        try {
            Files.createDirectories(file.toPath().getParent());

            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(file.toPath())) {
                bufferedWriter.write(csvManager.getHeadersForCSV());
                bufferedWriter.newLine();

                for (Task task : tasks.values()) {
                    bufferedWriter.write(csvManager.toString(task));
                    bufferedWriter.newLine();
                }

                for (Epic epic : epics.values()) {
                    bufferedWriter.write(csvManager.toString(epic));
                    bufferedWriter.newLine();
                }

                for (Subtask subtask : subtasks.values()) {
                    bufferedWriter.write(csvManager.toString(subtask));
                    bufferedWriter.newLine();
                }
            } catch (IOException exception) {
                throw new ManagerSaveException("Ошибка записи в файл: " + file.getName());
            }
        } catch (IOException exception) {
            throw new ManagerSaveException("Ошибка создания директории: " + file.getParent());
        }
    }

    @Override
    public ResultOfOperation addTask(Task task) {
        ResultOfOperation resultOfOperation = super.addTask(task);
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation removeAllTasks() {
        ResultOfOperation resultOfOperation = super.removeAllTasks();
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation updateTask(Task task) {
        ResultOfOperation resultOfOperation = super.updateTask(task);
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation removeTaskForIdNumber(int taskIdNumber) {
        ResultOfOperation resultOfOperation = super.removeTaskForIdNumber(taskIdNumber);
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation addEpic(Epic epic) {
        ResultOfOperation resultOfOperation = super.addEpic(epic);
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation updateEpicName(int epicIdNumber, String newEpicName) {
        ResultOfOperation resultOfOperation = super.updateEpicName(epicIdNumber, newEpicName);
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation updateEpicDescription(int epicIdNumber, String newEpicDescription) {
        ResultOfOperation resultOfOperation = super.updateEpicDescription(epicIdNumber, newEpicDescription);
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation removeAllEpics() {
        ResultOfOperation resultOfOperation = super.removeAllEpics();
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation removeEpicForIdNumber(int epicIdNumber) {
        ResultOfOperation resultOfOperation = super.removeEpicForIdNumber(epicIdNumber);
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation addSubtask(Subtask subtask) {
        ResultOfOperation resultOfOperation = super.addSubtask(subtask);
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation removeAllSubtasks() {
        ResultOfOperation resultOfOperation = super.removeAllSubtasks();
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation removeSubtaskForIdNumber(int subtaskIdNumber) {
        ResultOfOperation resultOfOperation = super.removeSubtaskForIdNumber(subtaskIdNumber);
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation updateSubtask(Subtask subtask) {
        ResultOfOperation resultOfOperation = super.updateSubtask(subtask);
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }
}
