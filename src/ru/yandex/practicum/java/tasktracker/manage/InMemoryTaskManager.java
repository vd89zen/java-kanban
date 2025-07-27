package ru.yandex.practicum.java.tasktracker.manage;

import ru.yandex.practicum.java.tasktracker.task.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    public static Integer counterForIdNumber;
    private static boolean isRestarted;
    private static Epic epicForWork;
    private final List<Integer> allIdInMemory;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    /*
     * "private final HashMap<Integer, Subtask> subtasks" привёл к единообразию с тасками и эпиками
     * (было: (ключ)ID подзадачи-(значение)ID её родителя), в том числе исходя из логики что главный по манипулияциям менеджер, и
     * за апдейты тоже он отвечает; + в эпике теперь только айди и статусы сабтасков, а не сами таски хранятся.
     */
    private final HashMap<Integer, Subtask> subtasks;
    private final HistoryManager historyManager;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        historyManager = ManagersUtil.getDefaultHistory();
        counterForIdNumber = 1;
        allIdInMemory = new ArrayList<>();
        isRestarted = false;
    }

    private ResultOfOperation generateIdNumber() {
        while (allIdInMemory.contains(counterForIdNumber)) {
            counterForIdNumber++;
            if (counterForIdNumber == Integer.MAX_VALUE) {
                if (isRestarted) {
                    return ResultOfOperation.ERROR_NO_AVAILABLE_ID;
                } else {
                    counterForIdNumber = 1;
                    isRestarted = true;
                }
            }
        }
        isRestarted = false;
        return ResultOfOperation.SUCCESS;
    }

    @Override
    public LinkedList<AbstractTask> getHistory() {
        return historyManager.getHistory();
    }

    //TASK
    @Override
    public ResultOfOperation addTask(Task task) {
        if (task == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (task.getName() == null || task.getDescription() == null
                || task.getStatusProgress() == null || task.getIdNumber() == null) {
            return ResultOfOperation.ERROR_OBJECT_FIELDS_NULL;
        } else if (task.getIdNumber() < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        } else if (allIdInMemory.contains(task.getIdNumber())) {
            return ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS;
        }

        if (task.getIdNumber() == 0) {
            if (generateIdNumber() == ResultOfOperation.SUCCESS) {
                task.setIdNumber(counterForIdNumber);
            } else {
                return ResultOfOperation.ERROR_NO_AVAILABLE_ID;
            }
        }

        tasks.put(task.getIdNumber(), task);
        allIdInMemory.add(task.getIdNumber());
        return ResultOfOperation.SUCCESS;
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        if (tasks.isEmpty()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(tasks.values());
    }

    @Override
    public ResultOfOperation removeAllTasks() {
        if (tasks.isEmpty()) {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        }

        allIdInMemory.removeAll(tasks.keySet());
        tasks.clear();
        return ResultOfOperation.SUCCESS;
    }

    @Override
    public Task getTaskForIdNumber(Integer taskIdNumber) {
        if (taskIdNumber == null || tasks.isEmpty()) {
            return new Task();
        }

        if (tasks.containsKey(taskIdNumber)) {
            historyManager.addRecord(new Task(tasks.get(taskIdNumber)));//на вход сразу передаём копию
            return tasks.get(taskIdNumber);
        }
        return new Task();
    }

    @Override
    public ResultOfOperation updateTask(Task task) {
        if (tasks.isEmpty()) {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        } else if (task == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (task.getName() == null || task.getDescription() == null
                || task.getStatusProgress() == null || task.getIdNumber() == null) {
            return ResultOfOperation.ERROR_OBJECT_FIELDS_NULL;
        }

        if (tasks.containsKey(task.getIdNumber())) {
            tasks.put(task.getIdNumber(), task);
            return ResultOfOperation.SUCCESS;
        }
        return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
    }

    @Override
    public ResultOfOperation removeTaskForIdNumber(Integer taskIdNumber) {
        if (taskIdNumber == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (tasks.isEmpty()) {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        }

        if (tasks.containsKey(taskIdNumber)) {
            tasks.remove(taskIdNumber);
            allIdInMemory.remove(taskIdNumber);
            return ResultOfOperation.SUCCESS;
        }
        return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
    }

    //EPIC
    @Override
    public ResultOfOperation addEpic(Epic epic) {
        if (epic == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (epic.getName() == null || epic.getDescription() == null
                || epic.getStatusProgress() == null || epic.getIdNumber() == null) {
            return ResultOfOperation.ERROR_OBJECT_FIELDS_NULL;
        } else if (epic.getIdNumber() < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        } else if (allIdInMemory.contains(epic.getIdNumber())) {
            return ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS;
        }

        if (epic.getIdNumber() == 0) {
            if (generateIdNumber() == ResultOfOperation.SUCCESS) {
                epic.setIdNumber(counterForIdNumber);
            } else {
                return ResultOfOperation.ERROR_NO_AVAILABLE_ID;
            }
        }

        if (epic.getAllSubtasksIdNumber().isEmpty() == false) {
            for (Integer subtaskIdNumber : epic.getAllSubtasksIdNumber()) {
                if (subtasks.containsKey(subtaskIdNumber) == false) {
                    return ResultOfOperation.ERROR_SUBTASK_NOT_FOUND;
                }
            }
        }

        epics.put(epic.getIdNumber(), epic);
        allIdInMemory.add(epic.getIdNumber());
        return ResultOfOperation.SUCCESS;
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        if (epics.isEmpty()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getAllSubtasksFromEpic(Integer epicIdNumber) {
        if (epicIdNumber == null || epics.isEmpty() || subtasks.isEmpty()) {
            return new ArrayList<>();
        }

        if (epics.containsKey(epicIdNumber)) {
            ArrayList<Subtask> allSubtasks = new ArrayList<>();
            for (Integer subtaskIdNumber : epics.get(epicIdNumber).getAllSubtasksIdNumber()) {
                allSubtasks.add(subtasks.get(subtaskIdNumber));
            }
            return allSubtasks;
        }
        return new ArrayList<>();
    }

    @Override
    public ResultOfOperation updateEpic(Epic epic) {
        if (epics.isEmpty()) {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        } else if (epic == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (epic.getName() == null || epic.getDescription() == null
                || epic.getStatusProgress() == null || epic.getIdNumber() == null) {
            return ResultOfOperation.ERROR_OBJECT_FIELDS_NULL;
        }

        if (epics.containsKey(epic.getIdNumber())) {
            if (epic.getAllSubtasksIdNumber().isEmpty() == false) {
                for (Integer subtaskIdNumber : epic.getAllSubtasksIdNumber()) {
                    if (subtasks.containsKey(subtaskIdNumber) == false) {
                        return ResultOfOperation.ERROR_SUBTASK_NOT_FOUND;
                    }
                }
            }
            epics.put(epic.getIdNumber(), epic);
            return ResultOfOperation.SUCCESS;
        }
        return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
    }

    @Override
    public ResultOfOperation removeAllEpics() {
        if (subtasks.isEmpty() == false) {
            allIdInMemory.removeAll(subtasks.keySet());
            subtasks.clear();
        }

        if (epics.isEmpty()) {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        }

        allIdInMemory.removeAll(epics.keySet());
        epics.clear();
        return ResultOfOperation.SUCCESS;
    }

    @Override
    public Epic getEpicForIdNumber(Integer epicIdNumber) {
        if (epicIdNumber == null || epics.isEmpty()) {
            return new Epic();
        }

        if (epics.containsKey(epicIdNumber)) {
            historyManager.addRecord(new Epic(epics.get(epicIdNumber)));
            return epics.get(epicIdNumber);
        }
        return new Epic();
    }

    @Override
    public ResultOfOperation removeEpicForIdNumber(Integer epicIdNumber) {
        if (epicIdNumber == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (epics.isEmpty()) {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        }

        if (epics.containsKey(epicIdNumber)) {
            if (subtasks.isEmpty() == false) {
                for (Integer subtaskIdNumber : epics.get(epicIdNumber).getAllSubtasksIdNumber()) {
                    subtasks.remove(subtaskIdNumber);
                    allIdInMemory.remove(subtaskIdNumber);
                }
            }

            epics.remove(epicIdNumber);
            allIdInMemory.remove(epicIdNumber);
            return ResultOfOperation.SUCCESS;
        }
        return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
    }

    //SUBTASK
    @Override
    public ResultOfOperation addSubtask(Subtask subtask) {
        if (subtask == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (subtask.getName() == null || subtask.getDescription() == null
                || subtask.getStatusProgress() == null || subtask.getIdNumber() == null) {
            return ResultOfOperation.ERROR_OBJECT_FIELDS_NULL;
        } else if (epics.isEmpty()) {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        } else if (subtask.getIdNumber() < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        } else if (allIdInMemory.contains(subtask.getIdNumber())) {
            return ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS;
        }

        if (epics.containsKey(subtask.getParentEpicIdNumber())) {
            if (subtask.getIdNumber() == 0) {
                if (generateIdNumber() == ResultOfOperation.SUCCESS) {
                    subtask.setIdNumber(counterForIdNumber);
                } else {
                    return ResultOfOperation.ERROR_NO_AVAILABLE_ID;
                }
            }
            epicForWork = epics.get(subtask.getParentEpicIdNumber());
            ResultOfOperation resultOfOperation = epicForWork.addSubtask(subtask.getIdNumber(), subtask.getStatusProgress());
            if (resultOfOperation == ResultOfOperation.SUCCESS) {
                subtasks.put(subtask.getIdNumber(), subtask);
                allIdInMemory.add(subtask.getIdNumber());
                return ResultOfOperation.SUCCESS;
            } else {
                return resultOfOperation;
            }
        }
        return ResultOfOperation.ERROR_MISMATCH_PARENT_ID;
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        if (epics.isEmpty() || subtasks.isEmpty()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(subtasks.values());
    }

    @Override
    public ResultOfOperation removeAllSubtasks() {
        if (epics.isEmpty() || subtasks.isEmpty()) {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        }

        for (Epic epic : epics.values()) {
            epic.removeAllSubtasksIdNumber();
        }

        allIdInMemory.removeAll(subtasks.keySet());
        subtasks.clear();
        return ResultOfOperation.SUCCESS;
    }

    @Override
    public Subtask getSubtaskForIdNumber(Integer subtaskIdNumber) {
        if (subtaskIdNumber == null || epics.isEmpty() || subtasks.isEmpty()) {
            return new Subtask();
        }

        if (subtasks.containsKey(subtaskIdNumber)) {
            historyManager.addRecord(new Subtask(subtasks.get(subtaskIdNumber)));
            return subtasks.get(subtaskIdNumber);
        }
        return new Subtask();
    }

    @Override
    public ResultOfOperation removeSubtaskForIdNumber(Integer subtaskIdNumber) {
        if (subtaskIdNumber == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (epics.isEmpty() || subtasks.isEmpty()) {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        }

        if (subtasks.containsKey(subtaskIdNumber)) {
            Integer parentEpicIdNumber = subtasks.get(subtaskIdNumber).getParentEpicIdNumber();
            subtasks.remove(subtaskIdNumber);
            allIdInMemory.remove(subtaskIdNumber);
            if (epics.containsKey(parentEpicIdNumber)) {
                epicForWork = epics.get(parentEpicIdNumber);
                return epicForWork.removeSubtask(subtaskIdNumber);
            }
        }
        return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
    }

    @Override
    public ResultOfOperation updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (subtask.getName() == null || subtask.getDescription() == null
                || subtask.getStatusProgress() == null || subtask.getIdNumber() == null) {
            return ResultOfOperation.ERROR_OBJECT_FIELDS_NULL;
        } else if (epics.isEmpty() || subtasks.isEmpty()) {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        } else if (subtask.getIdNumber() < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        }

        if (subtasks.containsKey(subtask.getIdNumber())) {
            Integer parentEpicIdNumber = subtask.getParentEpicIdNumber();
            if (epics.containsKey(parentEpicIdNumber)) {
                epicForWork = epics.get(parentEpicIdNumber);
                if (epicForWork.updateSubtask(subtask.getIdNumber(), subtask.getStatusProgress())
                        == ResultOfOperation.SUCCESS) {
                    subtasks.put(subtask.getIdNumber(), subtask);
                    return ResultOfOperation.SUCCESS;
                }
            }
        }
        return ResultOfOperation.ERROR_SUBTASK_NOT_FOUND;
    }

}
