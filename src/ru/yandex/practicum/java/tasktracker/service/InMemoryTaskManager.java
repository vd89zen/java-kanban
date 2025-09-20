package ru.yandex.practicum.java.tasktracker.service;

import ru.yandex.practicum.java.tasktracker.manage.ManagersUtil;
import ru.yandex.practicum.java.tasktracker.task.*;
import ru.yandex.practicum.java.tasktracker.utils.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private int counterForIdNumber;
    private boolean isRestarted;
    private Epic epicForWork;
    private Task taskForWork;
    private Subtask subtaskForWork;
    protected final HashSet<Integer> allIdInWork;
    protected final HashMap<Integer, Task> tasks;
    protected final HashMap<Integer, Epic> epics;
    protected final HashMap<Integer, Subtask> subtasks;
    protected final TreeSet<AbstractTask> prioritizedTasks;
    private final HistoryManager historyManager;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        historyManager = ManagersUtil.getDefaultHistory();
        counterForIdNumber = 1;
        allIdInWork = new HashSet<>();
        isRestarted = false;
        prioritizedTasks = new TreeSet<>((task1, task2) -> {
            if (task1.getIdNumber() == task2.getIdNumber()) {
                return 0;
            }

            int startCompare = task1.getStartDateTime().orElse(LocalDateTime.MIN)
                    .compareTo(task2.getStartDateTime().orElse(LocalDateTime.MIN));
            if (startCompare == 0) {
                return 0;
            }

            return startCompare;
        });
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

    private ResultOfOperation addToPriorityList(AbstractTask task) {
        if (task.getStartDateTime().isEmpty()) {
            return ResultOfOperation.ERROR_START_NOT_SET;
        }

        switch (task.getTypeTask()) {
            case TASK:
                if (prioritizedTasks.add(new Task((Task) task))) {
                    return ResultOfOperation.SUCCESS;
                } else {
                    return ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS;
                }
            case SUBTASK:
                if (prioritizedTasks.add(new Subtask((Subtask) task))) {
                    return ResultOfOperation.SUCCESS;
                } else {
                    return ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS;
                }
            default:
                return ResultOfOperation.ERROR_OBJECT_ILLEGAL;
        }
    }

    public boolean isTimeIntersectBoth(AbstractTask taskOne, AbstractTask taskTwo) {
        if (taskOne == null || taskTwo == null || taskOne.getStartDateTime().isEmpty()
                || taskTwo.getStartDateTime().isEmpty()) {
            return false;
        }

        return !(taskOne.getEndDateTime().get().isBefore(taskTwo.getStartDateTime().get())
                || taskOne.getStartDateTime().get().isAfter(taskTwo.getEndDateTime().get()));
    }

    @Override
    public boolean isTimeIntersectWithOthers(AbstractTask task) {
        if (task == null || task.getStartDateTime().isEmpty()) {
            return false;
        }

        return prioritizedTasks.stream()
                .anyMatch(prioritizedTask ->
                        task.getIdNumber() != prioritizedTask.getIdNumber()
                        ? isTimeIntersectBoth(task, prioritizedTask)
                        : false);
    }

    @Override
    public TreeSet<AbstractTask> getPrioritizedTasks() {
        return prioritizedTasks;
    }

    @Override
    public int getTotalOfIdInWork() {
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
        } else if (task.getName().isEmpty() || task.getDescription().isEmpty()
                || task.getStatusProgress() == null) {
            return ResultOfOperation.ERROR_OBJECT_FIELDS_NULL;
        } else if (task.getIdNumber() < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        } else if (allIdInWork.contains(task.getIdNumber())) {
            return ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS;
        } else if (isTimeIntersectWithOthers(task)) {
            return ResultOfOperation.ERROR_INTERSECT_TIME;
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
        addToPriorityList(taskForWork);
        taskForWork = null;
        return ResultOfOperation.SUCCESS;
    }

    @Override
    public Optional<ArrayList<Task>> getAllTasks() {
        if (tasks.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                tasks.values().stream()
                        .map(Task::new)
                        .collect(Collectors.toCollection(ArrayList::new))
        );
    }

    @Override
    public ResultOfOperation removeAllTasks() {
        if (tasks.isEmpty()) {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        }

        tasks.entrySet().forEach(entry -> {
                    historyManager.removeRecord(entry.getKey());
                    prioritizedTasks.remove(entry.getValue());
        });

        allIdInWork.removeAll(tasks.keySet());
        tasks.clear();
        return ResultOfOperation.SUCCESS;
    }

    @Override
    public Optional<Task> getTaskForIdNumber(int taskIdNumber) {
        if (taskIdNumber < 0 || tasks.isEmpty() || tasks.containsKey(taskIdNumber) == false) {
            return Optional.empty();
        }
        historyManager.addRecord(tasks.get(taskIdNumber));
        return Optional.of(new Task(tasks.get(taskIdNumber)));
    }

    @Override
    public ResultOfOperation updateTask(Task task) {
        if (task == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (task.getName().isEmpty() || task.getDescription().isEmpty()
                || task.getStatusProgress() == null) {
            return ResultOfOperation.ERROR_OBJECT_FIELDS_NULL;
        } else if (tasks.isEmpty() || tasks.containsKey(task.getIdNumber()) == false) {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        } else if (isTimeIntersectWithOthers(task)) {
            return ResultOfOperation.ERROR_INTERSECT_TIME;
        }

        prioritizedTasks.remove(tasks.get(task.getIdNumber()));
        taskForWork = new Task(task);
        tasks.put(taskForWork.getIdNumber(), taskForWork);
        addToPriorityList(taskForWork);
        taskForWork = null;
        return ResultOfOperation.SUCCESS;
    }

    @Override
    public ResultOfOperation removeTaskForIdNumber(int taskIdNumber) {
        if (taskIdNumber < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        } else if (tasks.isEmpty() || tasks.containsKey(taskIdNumber) == false) {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        }

        prioritizedTasks.remove(tasks.get(taskIdNumber));
        tasks.remove(taskIdNumber);
        historyManager.removeRecord(taskIdNumber);
        allIdInWork.remove(taskIdNumber);
        return ResultOfOperation.SUCCESS;
    }

    //EPIC
    @Override
    public ResultOfOperation addEpic(Epic epic) {
        if (epic == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (epic.getName() == null || epic.getDescription() == null
                || epic.getStatusProgress() == null) {
            return ResultOfOperation.ERROR_OBJECT_FIELDS_NULL;
        } else if (epic.getIdNumber() < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        } else if (allIdInWork.contains(epic.getIdNumber())) {
            return ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS;
        } else if (epic.getSubtasksIdNumber().isEmpty() == false) {
            return ResultOfOperation.ERROR_SUBTASKS_NOT_EMPTY;
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

        epics.put(epicForWork.getIdNumber(), epicForWork);
        allIdInWork.add(epicForWork.getIdNumber());
        epicForWork = null;
        return ResultOfOperation.SUCCESS;
    }

    @Override
    public Optional<ArrayList<Epic>> getAllEpics() {
        if (epics.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                epics.values().stream()
                        .map(Epic::new)
                        .collect(Collectors.toCollection(ArrayList::new))
        );
    }

    @Override
    public Optional<ArrayList<Subtask>> getAllSubtasksFromEpic(int epicIdNumber) {
        if (epicIdNumber < 0 || epics.isEmpty() || subtasks.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                subtasks.values().stream()
                        .filter(subtask -> subtask.getParentEpicIdNumber() == epicIdNumber)
                        .map(Subtask::new)
                        .collect(Collectors.toCollection(ArrayList::new))
        );
    }

    /*
     * Единственные два поля эпика которые можно валидно обновить вне менеджера, это имя и описание.
     * Расчётные поля (по сути все остальные, кроме айди и списка сабтасков) в соответствующих ситуациях автоматически
     * обновляются у экземпляра эпика, находящегося внутри менеджера. Всё что касается сабтасков эпика автоматически
     * обновляется через менеджера. То есть обновление эпика путём полной замены эпика внутри менеджера эпиком снаружи,
     * кроме возможности случайных(умышленных) багов (и множества проверок для их избежания) ничего не даёт.
     * Исходя из текущей сути эпика, он всего лишь вывеска, с описанием, для группы сабтасков, и полностью расчитывается
     * на их основе. После создания эпика, мы работаем дальше с сабтасками, смысл отдельного обновления эпика
     * фактически теряется (только если понадобится изменить имя или описание эпика).
     * Поэтому вместо метода updateEpic(Epic epic), введены два метода для обнобления имени и описания эпика.
     */
    @Override
    public ResultOfOperation updateEpicName(int epicIdNumber, String newEpicName) {
        if (epicIdNumber < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        } else if (newEpicName == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (epics.isEmpty() || epics.containsKey(epicIdNumber) == false) {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        }

        epics.get(epicIdNumber).setName(newEpicName);
        return ResultOfOperation.SUCCESS;
    }

    @Override
    public ResultOfOperation updateEpicDescription(int epicIdNumber, String newEpicDescription) {
        if (epicIdNumber < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        } else if (newEpicDescription == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (epics.isEmpty() || epics.containsKey(epicIdNumber) == false) {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        }

        epics.get(epicIdNumber).setDescription(newEpicDescription);
        return ResultOfOperation.SUCCESS;
    }

    @Override
    public ResultOfOperation removeAllEpics() {
        if (epics.isEmpty()) {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        }

        if (subtasks.isEmpty() == false) {
            allIdInWork.removeAll(subtasks.keySet());
            subtasks.entrySet().forEach(entry -> {
                historyManager.removeRecord(entry.getKey());
                prioritizedTasks.remove(entry.getValue());
            });
            subtasks.clear();
        }

        epics.keySet().forEach(id -> historyManager.removeRecord(id));
        allIdInWork.removeAll(epics.keySet());
        epics.clear();
        return ResultOfOperation.SUCCESS;
    }

    @Override
    public Optional<Epic> getEpicForIdNumber(int epicIdNumber) {
        if (epicIdNumber < 0 || epics.isEmpty() || epics.containsKey(epicIdNumber) == false) {
            return Optional.empty();
        }
        historyManager.addRecord(epics.get(epicIdNumber));
        return Optional.of(new Epic(epics.get(epicIdNumber)));
    }

    @Override
    public ResultOfOperation removeEpicForIdNumber(int epicIdNumber) {
        if (epicIdNumber < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        } else if (epics.isEmpty() || epics.containsKey(epicIdNumber) == false) {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        }

        if (subtasks.isEmpty() == false) {
            epics.get(epicIdNumber).getSubtasksIdNumber().orElseGet(ArrayList::new)
                    .forEach(subtaskIdNumber -> {
                        prioritizedTasks.remove(subtasks.get(subtaskIdNumber));
                        subtasks.remove(subtaskIdNumber);
                        allIdInWork.remove(subtaskIdNumber);
                        historyManager.removeRecord(subtaskIdNumber);
                    });
        }

        epics.remove(epicIdNumber);
        allIdInWork.remove(epicIdNumber);
        historyManager.removeRecord(epicIdNumber);
        return ResultOfOperation.SUCCESS;
    }

    //SUBTASK
    @Override
    public ResultOfOperation addSubtask(Subtask subtask) {
        if (subtask == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (subtask.getName().isEmpty() || subtask.getDescription().isEmpty()
                || subtask.getStatusProgress() == null) {
            return ResultOfOperation.ERROR_OBJECT_FIELDS_NULL;
        } else if (epics.isEmpty()) {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        } else if (subtask.getIdNumber() < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        } else if (allIdInWork.contains(subtask.getIdNumber())) {
            return ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS;
        } else if (epics.containsKey(subtask.getParentEpicIdNumber()) == false) {
            return ResultOfOperation.ERROR_MISMATCH_PARENT_ID;
        } else if (isTimeIntersectWithOthers(subtask)) {
            return ResultOfOperation.ERROR_INTERSECT_TIME;
        }

        subtaskForWork = new Subtask(subtask);

        if (subtaskForWork.getIdNumber() == 0) {
            if (generateIdNumber() == ResultOfOperation.SUCCESS) {
                subtaskForWork.setIdNumber(counterForIdNumber);
                subtask.setIdNumber(counterForIdNumber);
            } else {
                return ResultOfOperation.ERROR_NO_AVAILABLE_ID;
            }
        }

        epicForWork = epics.get(subtaskForWork.getParentEpicIdNumber());
        ResultOfOperation resultOfOperation = epicForWork.addSubtask(subtaskForWork);
        if (resultOfOperation == ResultOfOperation.SUCCESS) {
            subtasks.put(subtaskForWork.getIdNumber(), subtaskForWork);
            allIdInWork.add(subtaskForWork.getIdNumber());
            addToPriorityList(subtaskForWork);
            subtaskForWork = null;
            epicForWork = null;
            return ResultOfOperation.SUCCESS;
        } else {
            return resultOfOperation;
        }
    }

    @Override
    public Optional<ArrayList<Subtask>> getAllSubtasks() {
        if (epics.isEmpty() || subtasks.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                subtasks.values().stream()
                        .map(Subtask::new)
                        .collect(Collectors.toCollection(ArrayList::new))
        );
    }

    @Override
    public ResultOfOperation removeAllSubtasks() {
        if (epics.isEmpty() || subtasks.isEmpty()) {
            return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
        }

        epics.values().forEach(epic -> epic.removeAllSubtasks());
        subtasks.entrySet().forEach(entry -> {
            historyManager.removeRecord(entry.getKey());
            prioritizedTasks.remove(entry.getValue());
        });
        allIdInWork.removeAll(subtasks.keySet());
        subtasks.clear();
        return ResultOfOperation.SUCCESS;
    }

    @Override
    public Optional<Subtask> getSubtaskForIdNumber(int subtaskIdNumber) {
        if (subtaskIdNumber < 0 || epics.isEmpty() || subtasks.isEmpty()
                || subtasks.containsKey(subtaskIdNumber) == false) {
            return Optional.empty();
        }

        historyManager.addRecord(subtasks.get(subtaskIdNumber));
        return Optional.of(new Subtask(subtasks.get(subtaskIdNumber)));
    }

    @Override
    public ResultOfOperation removeSubtaskForIdNumber(int subtaskIdNumber) {
        if (subtaskIdNumber < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        } else if (epics.isEmpty() || subtasks.isEmpty() || subtasks.containsKey(subtaskIdNumber) == false) {
            return ResultOfOperation.ERROR_SUBTASK_NOT_FOUND;
        }

        int parentEpicIdNumber = subtasks.get(subtaskIdNumber).getParentEpicIdNumber();
        prioritizedTasks.remove(subtasks.get(subtaskIdNumber));
        subtasks.remove(subtaskIdNumber);
        historyManager.removeRecord(subtaskIdNumber);
        allIdInWork.remove(subtaskIdNumber);
        if (epics.containsKey(parentEpicIdNumber)) {
            return epics.get(parentEpicIdNumber).removeSubtask(subtaskIdNumber);
        } else {
            return ResultOfOperation.ERROR_MISMATCH_PARENT_ID;
        }
    }

    @Override
    public ResultOfOperation updateSubtask(Subtask subtask) {
        if (subtask == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (subtask.getName() == null || subtask.getDescription() == null
                || subtask.getStatusProgress() == null) {
            return ResultOfOperation.ERROR_OBJECT_FIELDS_NULL;
        }

        int subtaskIdNumber = subtask.getIdNumber();
        int subtaskParentEpicIdNumber = subtask.getParentEpicIdNumber();
        subtaskForWork = new Subtask(subtask);

        if (subtaskIdNumber < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        } else if (epics.isEmpty() || subtasks.isEmpty() || subtasks.containsKey(subtaskIdNumber) == false) {
            return ResultOfOperation.ERROR_SUBTASK_NOT_FOUND;
        } else if (epics.containsKey(subtaskParentEpicIdNumber) == false
                || subtasks.get(subtaskIdNumber).getParentEpicIdNumber() != subtaskParentEpicIdNumber) {
            return ResultOfOperation.ERROR_MISMATCH_PARENT_ID;
        } else if (isTimeIntersectWithOthers(subtaskForWork)) {
            return ResultOfOperation.ERROR_INTERSECT_TIME;
        }

        prioritizedTasks.remove(subtasks.get(subtaskIdNumber));
        subtasks.put(subtaskIdNumber, subtaskForWork);
        addToPriorityList(subtaskForWork);
        return epics.get(subtaskParentEpicIdNumber).updateSubtask(subtask);
    }
}
