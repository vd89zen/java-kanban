package ru.yandex.practicum.java.tasktracker.manage;

import ru.yandex.practicum.java.tasktracker.task.Epic;
import ru.yandex.practicum.java.tasktracker.task.Subtask;
import ru.yandex.practicum.java.tasktracker.task.Task;
import java.util.List;
import java.nio.file.Files;
import java.io.File;
import java.io.BufferedWriter;
import java.io.IOException;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private File file;
    private final CommaSeparatedValuesManager csvManager;

    public static FileBackedTaskManager loadFromFile(File file) {
        final byte HEADER_TYPE = 1;
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
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
            return fileBackedTaskManager;
        } else {
            return new FileBackedTaskManager(file);
        }
    }

    public FileBackedTaskManager (File file) {
        super();
        this.file = file;
        csvManager = new CommaSeparatedValuesManager();
    }

    public static void main(String[] args) throws IOException {
        /*
        * Дополнительное задание. Реализуем пользовательский сценарий.
        * 0. Создайте метод static void main(String[] args) в классе FileBackedTaskManager и реализуйте небольшой сценарий:
        * 1. Заведите несколько разных задач, эпиков и подзадач.
        * 2. Создайте новый FileBackedTaskManager-менеджер из этого же файла.
        * 3. Проверьте, что все задачи, эпики, подзадачи, которые были в старом менеджере, есть в новом.
        */

        File tempFile = File.createTempFile("temptasks", ".csv");

        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(tempFile);
        //1
        DataForUserScenario.fillData(fileBackedTaskManager);
        System.out.println("\nДанные из Менеджера созданного с нуля");
        System.out.println(fileBackedTaskManager.getAllTasks());
        System.out.println(fileBackedTaskManager.getAllEpics());
        System.out.println(fileBackedTaskManager.getAllSubtasks());
        //2
        FileBackedTaskManager fileBackedTaskManagerFromFile = loadFromFile(tempFile);
        //3
        System.out.println("\nДанные из Менеджера созданного из файла");
        System.out.println(fileBackedTaskManagerFromFile.getAllTasks());
        System.out.println(fileBackedTaskManagerFromFile.getAllEpics());
        System.out.println(fileBackedTaskManagerFromFile.getAllSubtasks());

    }

    private void save() {
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
            throw new ManagerSaveException("Ошибка создания директории: " + file.getParent().toString());
        }
    }

    @Override
    public ResultOfOperation addTask(Task task) {
        final ResultOfOperation resultOfOperation = super.addTask(task);
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation removeAllTasks() {
        final ResultOfOperation resultOfOperation = super.removeAllTasks();
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation updateTask(Task task) {
        final ResultOfOperation resultOfOperation = super.updateTask(task);
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation removeTaskForIdNumber(Integer taskIdNumber) {
        final ResultOfOperation resultOfOperation = super.removeTaskForIdNumber(taskIdNumber);
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation addEpic(Epic epic) {
        final ResultOfOperation resultOfOperation = super.addEpic(epic);
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation updateEpic(Epic epic) {
        final ResultOfOperation resultOfOperation = super.updateEpic(epic);
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation removeAllEpics() {
        final ResultOfOperation resultOfOperation = super.removeAllEpics();
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation removeEpicForIdNumber(Integer epicIdNumber) {
        final ResultOfOperation resultOfOperation = super.removeEpicForIdNumber(epicIdNumber);
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation addSubtask(Subtask subtask) {
        final ResultOfOperation resultOfOperation = super.addSubtask(subtask);
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation removeAllSubtasks() {
        final ResultOfOperation resultOfOperation = super.removeAllSubtasks();
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation removeSubtaskForIdNumber(Integer subtaskIdNumber) {
        final ResultOfOperation resultOfOperation = super.removeSubtaskForIdNumber(subtaskIdNumber);
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }

    @Override
    public ResultOfOperation updateSubtask(Subtask subtask) {
        final ResultOfOperation resultOfOperation = super.updateSubtask(subtask);
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            save();
        }
        return resultOfOperation;
    }
}
