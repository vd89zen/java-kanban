package ru.yandex.practicum.java.tasktracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.java.tasktracker.exceptions.NotFoundException;
import ru.yandex.practicum.java.tasktracker.manage.ManagersUtil;
import ru.yandex.practicum.java.tasktracker.storage.LoadedState;
import ru.yandex.practicum.java.tasktracker.storage.NoopStorage;
import ru.yandex.practicum.java.tasktracker.storage.TaskStorage;
import ru.yandex.practicum.java.tasktracker.task.*;
import ru.yandex.practicum.java.tasktracker.utils.*;
import ru.yandex.practicum.java.tasktracker.utils.enums.ResultOfOperation;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/**
 * Менеджер задач: держит состояние в памяти, делегирует персистенцию в {@link TaskStorage}.
 *
 * <p><b>Потокобезопасность.</b> Все мутационные методы выполняются под единым внутренним
 * локом ({@code lock}). Читающие методы лок не захватывают — коллекции представлены
 * конкурентными реализациями ({@code ConcurrentHashMap}, {@code ConcurrentSkipListSet}),
 * поэтому одиночные операции чтения атомарны.
 *
 * <p><b>Атомарность.</b> Под локом выполняются:
 * <ul>
 *   <li>проверки, зависящие от общего состояния (уникальность id, пересечение интервалов,
 *       наличие родительского эпика);</li>
 *   <li>генерация нового id и присвоение его объекту;</li>
 *   <li>все изменения {@code tasks}, {@code epics}, {@code subtasks}, {@code allIdInWork},
 *       {@code prioritizedTasks}.</li>
 * </ul>
 *
 * <p><b>Персистенция.</b> {@code persist()} вызывается вне лока — чтобы не держать
 * блокировку во время I/O. Состояние на момент вызова согласовано: все мутации уже
 * применены под локом. Возможна ситуация, когда между снятием лока и вызовом
 * {@code persist()} другой поток успеет внести своё изменение — тогда файл будет
 * записан с частично устаревшим состоянием. В текущей архитектуре это допустимо:
 * при следующей успешной мутации файл перезапишется.
 *
 * <p><b>Порядок захвата локов.</b> Если под локом менеджера вызываются методы
 * {@link TaskViewHistory}, они дополнительно синхронизируются на собственном мониторе.
 * Порядок всегда: <i>сначала lock менеджера, затем монитор истории</i>. Обратного
 * пути нет — {@code TaskViewHistory} не знает о {@code TaskManager}. Это исключает
 * циклическое ожидание (deadlock).
 */
public class TaskManager {
    private static final Logger log = LoggerFactory.getLogger(TaskManager.class);
    private int nextId;
    protected final Set<Integer> allIdInWork;
    protected final Map<Integer, Task> tasks;
    protected final Map<Integer, Epic> epics;
    protected final Map<Integer, Subtask> subtasks;
    protected final NavigableSet<AbstractTask> prioritizedTasks;
    private final TaskViewHistory historyManager;
    private final TaskStorage storage;
    private final Object lock = new Object();

    public TaskManager() {
        this(new NoopStorage());
    }

    public TaskManager(TaskStorage storage) {
        this.storage = Objects.requireNonNull(storage, "'storage' can't be null");
        tasks = new ConcurrentHashMap<>();
        epics = new ConcurrentHashMap<>();
        subtasks = new ConcurrentHashMap<>();
        historyManager = ManagersUtil.getDefaultHistory();
        nextId = 0;
        allIdInWork = ConcurrentHashMap.newKeySet();
        prioritizedTasks = new ConcurrentSkipListSet<>((task1, task2) -> {
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
        initFromStorage();
    }

    private void initFromStorage() {
        LoadedState state = storage.load();

        for (Task task : state.tasks()) {
            Task copy = new Task(task);
            tasks.put(copy.getIdNumber(), copy);
            allIdInWork.add(copy.getIdNumber());
            nextId = Math.max(nextId, copy.getIdNumber());
        }
        for (Epic epic : state.epics()) {
            Epic copy = new Epic(epic);
            epics.put(copy.getIdNumber(), copy);
            allIdInWork.add(copy.getIdNumber());
            nextId = Math.max(nextId, copy.getIdNumber());
        }
        for (Subtask subtask : state.subtasks()) {
            Subtask copy = new Subtask(subtask);
            subtasks.put(copy.getIdNumber(), copy);
            allIdInWork.add(copy.getIdNumber());
            nextId = Math.max(nextId, copy.getIdNumber());
        }

        for (Subtask subtask : subtasks.values()) {
            Epic parent = epics.get(subtask.getParentEpicIdNumber());
            if (parent != null) {
                parent.addSubtask(subtask);
            }
        }

        for (Task task : tasks.values()) {
            if (task.getStartDateTime().isPresent()) {
                prioritizedTasks.add(new Task(task));
            }
        }
        for (Subtask subtask : subtasks.values()) {
            if (subtask.getStartDateTime().isPresent()) {
                prioritizedTasks.add(new Subtask(subtask));
            }
        }

        verifyIntegrity();
    }

    private void persist() {
        storage.save(tasks.values(), epics.values(), subtasks.values());
    }

    private ResultOfOperation generateIdNumber() {
        if (nextId == Integer.MAX_VALUE) {
            return ResultOfOperation.ERROR_NO_AVAILABLE_ID;
        }
        nextId++;
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

    private void verifyIntegrity() {
        List<Subtask> orphanSubtasks = subtasks.values().stream()
                .filter(subtask -> !epics.containsKey(subtask.getParentEpicIdNumber()))
                .toList();

        if (orphanSubtasks.isEmpty()) {
            return;
        }

        log.warn("Found {} orphan subtask(s) referencing non-existent epic. Removing.", orphanSubtasks.size());
        for (Subtask subtask : orphanSubtasks) {
            log.warn("  Subtask id={} '{}' references epic id={}",
                    subtask.getIdNumber(),
                    subtask.getName().orElse("null"),
                    subtask.getParentEpicIdNumber());
            subtasks.remove(subtask.getIdNumber());
            allIdInWork.remove(subtask.getIdNumber());
            prioritizedTasks.removeIf(task -> task.getIdNumber() == subtask.getIdNumber());
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

    public ArrayList<AbstractTask> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    public int getTotalOfIdInWork() {
        return allIdInWork.size();
    }

    public ArrayList<AbstractTask> getHistory() {
        return historyManager.getHistory();
    }

    //TASK
    public ResultOfOperation addTask(Task task) {
        if (task == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (task.getName().isEmpty() || task.getDescription().isEmpty()
                || task.getStatusProgress() == null) {
            return ResultOfOperation.ERROR_OBJECT_FIELDS_NULL;
        } else if (task.getIdNumber() < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        }

        Task taskForWork = new Task(task);

        synchronized (lock) {
            if (allIdInWork.contains(taskForWork.getIdNumber())) {
                return ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS;
            }
            if (isTimeIntersectWithOthers(taskForWork)) {
                return ResultOfOperation.ERROR_INTERSECT_TIME;
            }

            if (taskForWork.getIdNumber() == 0) {
                if (generateIdNumber() == ResultOfOperation.SUCCESS) {
                    taskForWork.setIdNumber(nextId);
                    task.setIdNumber(nextId);
                } else {
                    return ResultOfOperation.ERROR_NO_AVAILABLE_ID;
                }
            } else {
                nextId = Math.max(nextId, taskForWork.getIdNumber());
            }

            tasks.put(taskForWork.getIdNumber(), taskForWork);
            allIdInWork.add(taskForWork.getIdNumber());
            addToPriorityList(taskForWork);
        }

        persist();
        return ResultOfOperation.SUCCESS;
    }

    public ArrayList<Task> getAllTasks() {
        return tasks.values().stream()
                .map(Task::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ResultOfOperation removeAllTasks() {
        synchronized (lock) {
            if (tasks.isEmpty()) {
                return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
            }

            tasks.entrySet().forEach(entry -> {
                historyManager.removeRecord(entry.getKey());
                prioritizedTasks.remove(entry.getValue());
            });

            allIdInWork.removeAll(tasks.keySet());
            tasks.clear();
        }

        persist();
        return ResultOfOperation.SUCCESS;
    }

    public Task getTaskByIdNumber(int taskIdNumber) throws NotFoundException {
        if (taskIdNumber <= 0 || tasks.isEmpty() || tasks.containsKey(taskIdNumber) == false) {
            throw new NotFoundException(String.format("Task (id: %d) for 'get' not found", taskIdNumber));
        }
        historyManager.addRecord(tasks.get(taskIdNumber));
        return new Task(tasks.get(taskIdNumber));
    }

    public ResultOfOperation updateTask(Task task) throws NotFoundException {
        if (task == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (task.getName().isEmpty() || task.getDescription().isEmpty()
                || task.getStatusProgress() == null) {
            return ResultOfOperation.ERROR_OBJECT_FIELDS_NULL;
        }

        synchronized (lock) {
            if (task.getIdNumber() <= 0 || tasks.isEmpty() || tasks.containsKey(task.getIdNumber()) == false) {
                throw new NotFoundException(String.format("Task (id: %d) for 'update' not found", task.getIdNumber()));
            }
            if (isTimeIntersectWithOthers(task)) {
                return ResultOfOperation.ERROR_INTERSECT_TIME;
            }

            prioritizedTasks.remove(tasks.get(task.getIdNumber()));
            Task taskForWork = new Task(task);
            tasks.put(taskForWork.getIdNumber(), taskForWork);
            addToPriorityList(taskForWork);
        }

        persist();
        return ResultOfOperation.SUCCESS;
    }

    public ResultOfOperation removeTaskByIdNumber(int taskIdNumber) throws NotFoundException {
        synchronized (lock) {
            if (taskIdNumber <= 0 || tasks.isEmpty() || tasks.containsKey(taskIdNumber) == false) {
                throw new NotFoundException(String.format("Task (id: %d) for 'remove' not found", taskIdNumber));
            }

            prioritizedTasks.remove(tasks.get(taskIdNumber));
            tasks.remove(taskIdNumber);
            historyManager.removeRecord(taskIdNumber);
            allIdInWork.remove(taskIdNumber);
        }

        persist();
        return ResultOfOperation.SUCCESS;
    }

    //EPIC
    public ResultOfOperation addEpic(Epic epic) {
        if (epic == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (epic.getName().isEmpty() || epic.getDescription().isEmpty()
                || epic.getStatusProgress() == null) {
            return ResultOfOperation.ERROR_OBJECT_FIELDS_NULL;
        } else if (epic.getIdNumber() < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        }

        Epic epicForWork;

        synchronized (lock) {
            if (allIdInWork.contains(epic.getIdNumber())) {
                return ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS;
            }
            if (epic.getSubtasksIdNumber().isEmpty() == false) {
                return ResultOfOperation.ERROR_SUBTASKS_NOT_EMPTY;
            }

            if (epic.getIdNumber() == 0) {
                if (epic.getDurationInMinutes() != 0 || epic.getStartDateTime().isPresent()
                        || epic.getEndDateTime().isPresent()) {
                    return ResultOfOperation.ERROR_OBJECT_ILLEGAL;
                }
                epicForWork = new Epic(epic.getName().get(), epic.getDescription().get());
                if (generateIdNumber() == ResultOfOperation.SUCCESS) {
                    epicForWork.setIdNumber(nextId);
                    epic.setIdNumber(nextId);
                } else {
                    return ResultOfOperation.ERROR_NO_AVAILABLE_ID;
                }
            } else {
                epicForWork = new Epic(epic);
                nextId = Math.max(nextId, epicForWork.getIdNumber());
            }

            epics.put(epicForWork.getIdNumber(), epicForWork);
            allIdInWork.add(epicForWork.getIdNumber());
        }

        persist();
        return ResultOfOperation.SUCCESS;
    }

    public ArrayList<Epic> getAllEpics() {
        return epics.values().stream()
                .map(Epic::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<Subtask> getAllSubtasksFromEpic(int epicIdNumber) throws NotFoundException {
        if (epicIdNumber <= 0 || epics.isEmpty() || epics.containsKey(epicIdNumber) == false) {
            throw new NotFoundException(String.format("Epic (id: %d) for 'get subtasks' not found", epicIdNumber));
        }

        return epics.get(epicIdNumber).getSubtasksIdNumber()
                .orElseGet(ArrayList::new)
                .stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .map(Subtask::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ResultOfOperation updateEpicName(int epicIdNumber, String newEpicName) {
        if (epicIdNumber < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        } else if (newEpicName == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        }

        synchronized (lock) {
            if (epics.isEmpty() || epics.containsKey(epicIdNumber) == false) {
                return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
            }
            epics.get(epicIdNumber).setName(newEpicName);
        }

        persist();
        return ResultOfOperation.SUCCESS;
    }

    public ResultOfOperation updateEpicDescription(int epicIdNumber, String newEpicDescription) {
        if (epicIdNumber < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        } else if (newEpicDescription == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        }

        synchronized (lock) {
            if (epics.isEmpty() || epics.containsKey(epicIdNumber) == false) {
                return ResultOfOperation.ERROR_OBJECT_NOT_FOUND;
            }
            epics.get(epicIdNumber).setDescription(newEpicDescription);
        }

        persist();
        return ResultOfOperation.SUCCESS;
    }

    public ResultOfOperation removeAllEpics() {
        synchronized (lock) {
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
        }

        persist();
        return ResultOfOperation.SUCCESS;
    }

    public Epic getEpicByIdNumber(int epicIdNumber) throws NotFoundException {
        if (epicIdNumber <= 0 || epics.isEmpty() || epics.containsKey(epicIdNumber) == false) {
            throw new NotFoundException(String.format("Epic (id: %d) for 'get' not found", epicIdNumber));
        }
        historyManager.addRecord(epics.get(epicIdNumber));
        return new Epic(epics.get(epicIdNumber));
    }

    public ResultOfOperation removeEpicByIdNumber(int epicIdNumber) {
        synchronized (lock) {
            if (epicIdNumber <= 0 || epics.isEmpty() || epics.containsKey(epicIdNumber) == false) {
                throw new NotFoundException(String.format("Epic (id: %d) for 'remove' not found", epicIdNumber));
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
        }

        persist();
        return ResultOfOperation.SUCCESS;
    }

    //SUBTASK
    public ResultOfOperation addSubtask(Subtask subtask) {
        if (subtask == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (subtask.getName().isEmpty() || subtask.getDescription().isEmpty()
                || subtask.getStatusProgress() == null) {
            return ResultOfOperation.ERROR_OBJECT_FIELDS_NULL;
        } else if (subtask.getIdNumber() < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        }

        synchronized (lock) {
            if (allIdInWork.contains(subtask.getIdNumber())) {
                return ResultOfOperation.ERROR_OBJECT_ALREADY_EXISTS;
            }
            if (epics.isEmpty() || epics.containsKey(subtask.getParentEpicIdNumber()) == false) {
                return ResultOfOperation.ERROR_MISMATCH_PARENT_ID;
            }
            if (isTimeIntersectWithOthers(subtask)) {
                return ResultOfOperation.ERROR_INTERSECT_TIME;
            }

            Subtask subtaskForWork = new Subtask(subtask);

            if (subtaskForWork.getIdNumber() == 0) {
                if (generateIdNumber() == ResultOfOperation.SUCCESS) {
                    subtaskForWork.setIdNumber(nextId);
                    subtask.setIdNumber(nextId);
                } else {
                    return ResultOfOperation.ERROR_NO_AVAILABLE_ID;
                }
            } else {
                nextId = Math.max(nextId, subtaskForWork.getIdNumber());
            }

            Epic epicForWork = epics.get(subtaskForWork.getParentEpicIdNumber());
            ResultOfOperation resultOfOperation = epicForWork.addSubtask(subtaskForWork);
            if (resultOfOperation == ResultOfOperation.SUCCESS) {
                subtasks.put(subtaskForWork.getIdNumber(), subtaskForWork);
                allIdInWork.add(subtaskForWork.getIdNumber());
                addToPriorityList(subtaskForWork);
            } else {
                return resultOfOperation;
            }
        }

        persist();
        return ResultOfOperation.SUCCESS;
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return subtasks.values().stream()
                .map(Subtask::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ResultOfOperation removeAllSubtasks() {
        synchronized (lock) {
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
        }

        persist();
        return ResultOfOperation.SUCCESS;
    }

    public Subtask getSubtaskByIdNumber(int subtaskIdNumber) throws NotFoundException {
        if (subtaskIdNumber <= 0 || epics.isEmpty() || subtasks.isEmpty()
                || subtasks.containsKey(subtaskIdNumber) == false) {
            throw new NotFoundException(String.format("Subtask (id: %d) for 'get' not found", subtaskIdNumber));
        }

        historyManager.addRecord(subtasks.get(subtaskIdNumber));
        return new Subtask(subtasks.get(subtaskIdNumber));
    }


    public ResultOfOperation removeSubtaskByIdNumber(int subtaskIdNumber) throws NotFoundException {
        synchronized (lock) {
            if (subtaskIdNumber <= 0 || epics.isEmpty() || subtasks.isEmpty()
                    || subtasks.containsKey(subtaskIdNumber) == false) {
                throw new NotFoundException(String.format("Subtask (id: %d) for 'remove' not found", subtaskIdNumber));
            }

            int parentEpicIdNumber = subtasks.get(subtaskIdNumber).getParentEpicIdNumber();
            prioritizedTasks.remove(subtasks.get(subtaskIdNumber));
            subtasks.remove(subtaskIdNumber);
            historyManager.removeRecord(subtaskIdNumber);
            allIdInWork.remove(subtaskIdNumber);

            if (epics.containsKey(parentEpicIdNumber)) {
                ResultOfOperation result = epics.get(parentEpicIdNumber).removeSubtask(subtaskIdNumber);
                if (result != ResultOfOperation.SUCCESS) {
                    return result;
                }
            } else {
                throw new NotFoundException(String.format("Subtask (id: %d) for 'remove' not found: %s",
                        subtaskIdNumber, ResultOfOperation.ERROR_MISMATCH_PARENT_ID.name()));
            }
        }

        persist();
        return ResultOfOperation.SUCCESS;
    }

    public ResultOfOperation updateSubtask(Subtask subtask) throws NotFoundException {
        if (subtask == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (subtask.getName().isEmpty() || subtask.getDescription().isEmpty()
                || subtask.getStatusProgress() == null) {
            return ResultOfOperation.ERROR_OBJECT_FIELDS_NULL;
        }

        int subtaskIdNumber = subtask.getIdNumber();
        int subtaskParentEpicIdNumber = subtask.getParentEpicIdNumber();
        Subtask subtaskForWork = new Subtask(subtask);

        synchronized (lock) {
            if (subtaskIdNumber <= 0 || epics.isEmpty() || subtasks.isEmpty()
                    || subtasks.containsKey(subtaskIdNumber) == false) {
                throw new NotFoundException(String.format("Subtask (id: %d) for 'update' not found", subtask.getIdNumber()));
            }
            if (epics.containsKey(subtaskParentEpicIdNumber) == false
                    || subtasks.get(subtaskIdNumber).getParentEpicIdNumber() != subtaskParentEpicIdNumber) {
                return ResultOfOperation.ERROR_MISMATCH_PARENT_ID;
            }
            if (isTimeIntersectWithOthers(subtaskForWork)) {
                return ResultOfOperation.ERROR_INTERSECT_TIME;
            }

            prioritizedTasks.remove(subtasks.get(subtaskIdNumber));
            subtasks.put(subtaskIdNumber, subtaskForWork);
            addToPriorityList(subtaskForWork);
            ResultOfOperation result = epics.get(subtaskParentEpicIdNumber).updateSubtask(subtask);
            if (result != ResultOfOperation.SUCCESS) {
                return result;
            }
        }

        persist();
        return ResultOfOperation.SUCCESS;
    }
}
