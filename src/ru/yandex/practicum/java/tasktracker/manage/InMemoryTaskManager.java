package ru.yandex.practicum.java.tasktracker.manage;

import ru.yandex.practicum.java.tasktracker.task.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class InMemoryTaskManager implements TaskManager {
    private Integer counterForIdNumber;
    private boolean isRestarted;
    private Epic epicForWork;
    private Task taskForWork;
    private Subtask subtaskForWork;
    protected final HashSet<Integer> allIdInWork;
    protected final HashMap<Integer, Task> tasks;
    protected final HashMap<Integer, Epic> epics;
    protected final HashMap<Integer, Subtask> subtasks;
    private final HistoryManager historyManager;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        historyManager = ManagersUtil.getDefaultHistory();
        counterForIdNumber = 1;
        allIdInWork = new HashSet<>();
        isRestarted = false;
    }

    private ResultOfOperation generateIdNumber() {
        while (allIdInWork.contains(counterForIdNumber)) {
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
    public int getTotalOfIdNumber() {
        return allIdInWork.size();
    }

    @Override
    public ArrayList<AbstractTask> getHistory() {
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
        } else if (allIdInWork.contains(task.getIdNumber())) {
            return ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS;
        }

        taskForWork = new Task(task);

        if (taskForWork.getIdNumber() == 0) {
            if (generateIdNumber() == ResultOfOperation.SUCCESS) {
                taskForWork.setIdNumber(counterForIdNumber);
                task.setIdNumber(counterForIdNumber);
            } else {
                return ResultOfOperation.ERROR_NO_AVAILABLE_ID;
            }
        }

        tasks.put(taskForWork.getIdNumber(), taskForWork);
        allIdInWork.add(taskForWork.getIdNumber());
        taskForWork = null;
        return ResultOfOperation.SUCCESS;
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        if (tasks.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<Task> tasksForReturn = new ArrayList<>();
        for (Task task : tasks.values()) {
            tasksForReturn.add(new Task(task));
        }
        return tasksForReturn;
    }

    @Override
    public ResultOfOperation removeAllTasks() {
        if (tasks.isEmpty()) {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        }

        for (Integer idNumber : tasks.keySet()) {
            historyManager.removeRecord(idNumber);
        }

        allIdInWork.removeAll(tasks.keySet());
        tasks.clear();

        return ResultOfOperation.SUCCESS;
    }

    @Override
    public Task getTaskForIdNumber(Integer taskIdNumber) {
        if (taskIdNumber == null || tasks.isEmpty()) {
            return new Task();
        }

        if (tasks.containsKey(taskIdNumber)) {
            historyManager.addRecord(tasks.get(taskIdNumber));//т.к. дубли уже не нужны, просто передаём оригинал
            return new Task(tasks.get(taskIdNumber));
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
            taskForWork = new Task(task);
            tasks.put(taskForWork.getIdNumber(), taskForWork);
            taskForWork = null;
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
            historyManager.removeRecord(taskIdNumber);
            allIdInWork.remove(taskIdNumber);
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
        } else if (allIdInWork.contains(epic.getIdNumber())) {
            return ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS;
        }

        epicForWork = new Epic(epic);

        if (epicForWork.getIdNumber() == 0) {
            if (generateIdNumber() == ResultOfOperation.SUCCESS) {
                epicForWork.setIdNumber(counterForIdNumber);
                epic.setIdNumber(counterForIdNumber);
            } else {
                return ResultOfOperation.ERROR_NO_AVAILABLE_ID;
            }
        }

        if (epicForWork.getSubtasksIdNumber().isEmpty() == false) {
            for (Integer subtaskIdNumber : epicForWork.getSubtasksIdNumber()) {
                if (subtasks.containsKey(subtaskIdNumber) == false) {
                    return ResultOfOperation.ERROR_SUBTASK_NOT_FOUND;
                }
            }
        }

        epics.put(epicForWork.getIdNumber(), epicForWork);
        allIdInWork.add(epicForWork.getIdNumber());
        epicForWork = null;
        return ResultOfOperation.SUCCESS;
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        if (epics.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<Epic> epicsForReturn = new ArrayList<>();
        for (Epic epic : epics.values()) {
            epicsForReturn.add(new Epic(epic));
        }
        return epicsForReturn;
    }

    @Override
    public ArrayList<Subtask> getAllSubtasksFromEpic(Integer epicIdNumber) {
        if (epicIdNumber == null || epics.isEmpty() || subtasks.isEmpty()) {
            return new ArrayList<>();
        }

        if (epics.containsKey(epicIdNumber)) {
            ArrayList<Subtask> allSubtasks = new ArrayList<>();
            for (Integer subtaskIdNumber : epics.get(epicIdNumber).getSubtasksIdNumber()) {
                allSubtasks.add(new Subtask(subtasks.get(subtaskIdNumber)));
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
            epicForWork = new Epic(epic);
            if (epicForWork.getSubtasksIdNumber().isEmpty() == false) {
                for (Integer subtaskIdNumber : epicForWork.getSubtasksIdNumber()) {
                    if (subtasks.containsKey(subtaskIdNumber) == false) { //т.е.подзадача в эпик добавляется через Менеджера, иначе ошибка.
                        return ResultOfOperation.ERROR_SUBTASK_NOT_FOUND;
                    }
                }
            }
            epics.put(epicForWork.getIdNumber(), epicForWork);
            epicForWork = null;
            return ResultOfOperation.SUCCESS;
        }
        return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
    }

    @Override
    public ResultOfOperation removeAllEpics() {
        if (subtasks.isEmpty() == false) {
            allIdInWork.removeAll(subtasks.keySet());
            for (Integer idNumber : subtasks.keySet()) {
                historyManager.removeRecord(idNumber);
            }
            subtasks.clear();
        }

        if (epics.isEmpty()) {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        }

        for (Integer idNumber : epics.keySet()) {
            historyManager.removeRecord(idNumber);
        }

        allIdInWork.removeAll(epics.keySet());
        epics.clear();
        return ResultOfOperation.SUCCESS;
    }

    @Override
    public Epic getEpicForIdNumber(Integer epicIdNumber) {
        if (epicIdNumber == null || epics.isEmpty()) {
            return new Epic();
        }

        if (epics.containsKey(epicIdNumber)) {
            historyManager.addRecord(epics.get(epicIdNumber));
            return new Epic(epics.get(epicIdNumber));
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
                for (Integer subtaskIdNumber : epics.get(epicIdNumber).getSubtasksIdNumber()) {
                    subtasks.remove(subtaskIdNumber);
                    allIdInWork.remove(subtaskIdNumber);
                    historyManager.removeRecord(subtaskIdNumber);
                }
            }

            epics.remove(epicIdNumber);
            allIdInWork.remove(epicIdNumber);
            historyManager.removeRecord(epicIdNumber);
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
        } else if (allIdInWork.contains(subtask.getIdNumber())) {
            return ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS;
        }

        subtaskForWork = new Subtask(subtask);

        if (epics.containsKey(subtaskForWork.getParentEpicIdNumber())) {
            if (subtaskForWork.getIdNumber() == 0) {
                if (generateIdNumber() == ResultOfOperation.SUCCESS) {
                    subtaskForWork.setIdNumber(counterForIdNumber);
                    subtask.setIdNumber(counterForIdNumber);
                } else {
                    return ResultOfOperation.ERROR_NO_AVAILABLE_ID;
                }
            }
            epicForWork = epics.get(subtaskForWork.getParentEpicIdNumber());
            ResultOfOperation resultOfOperation = epicForWork.addSubtask(subtaskForWork.getIdNumber(),
                    subtaskForWork.getStatusProgress());
            if (resultOfOperation == ResultOfOperation.SUCCESS) {
                subtasks.put(subtaskForWork.getIdNumber(), subtaskForWork);
                allIdInWork.add(subtaskForWork.getIdNumber());
                subtaskForWork = null;
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

        ArrayList<Subtask> subtasksForReturn = new ArrayList<>();
        for (Subtask subtask : subtasks.values()) {
            subtasksForReturn.add(new Subtask(subtask));
        }
        return subtasksForReturn;
    }

    @Override
    public ResultOfOperation removeAllSubtasks() {
        if (epics.isEmpty() || subtasks.isEmpty()) {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        }

        for (Epic epic : epics.values()) {
            epic.removeAllSubtasksIdNumber();
        }

        for (Integer idNumber : subtasks.keySet()) {
            historyManager.removeRecord(idNumber);
        }

        allIdInWork.removeAll(subtasks.keySet());
        subtasks.clear();
        return ResultOfOperation.SUCCESS;
    }

    @Override
    public Subtask getSubtaskForIdNumber(Integer subtaskIdNumber) {
        if (subtaskIdNumber == null || epics.isEmpty() || subtasks.isEmpty()) {
            return new Subtask();
        }

        if (subtasks.containsKey(subtaskIdNumber)) {
            historyManager.addRecord(subtasks.get(subtaskIdNumber));
            return new Subtask(subtasks.get(subtaskIdNumber));
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
            historyManager.removeRecord(subtaskIdNumber);
            allIdInWork.remove(subtaskIdNumber);
            if (epics.containsKey(parentEpicIdNumber)) {
                return epics.get(parentEpicIdNumber).removeSubtask(subtaskIdNumber);
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
            subtaskForWork = subtask;
            Integer parentEpicIdNumber = subtaskForWork.getParentEpicIdNumber();
            if (epics.containsKey(parentEpicIdNumber)) {
                epicForWork = epics.get(parentEpicIdNumber);
                if (epicForWork.updateSubtask(subtaskForWork.getIdNumber(), subtaskForWork.getStatusProgress())
                        == ResultOfOperation.SUCCESS) {
                    subtasks.put(subtaskForWork.getIdNumber(), subtaskForWork);
                    subtaskForWork = null;
                    return ResultOfOperation.SUCCESS;
                }
            }
        }
        return ResultOfOperation.ERROR_SUBTASK_NOT_FOUND;
    }

}
