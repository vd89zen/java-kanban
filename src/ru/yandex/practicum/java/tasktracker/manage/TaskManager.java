package ru.yandex.practicum.java.tasktracker.manage;

import ru.yandex.practicum.java.tasktracker.task.Epic;
import ru.yandex.practicum.java.tasktracker.task.Task;
import ru.yandex.practicum.java.tasktracker.task.Subtask;
import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private static int counterForIdNumber;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, Integer> subtasks; // (ключ)ID подзадачи-(значение)ID её родителя
    private static Epic epicForWork;

    public TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        counterForIdNumber = 0;
    }

//TASK
    public boolean addTask(Task task) {
        if (task == null) {
            return false;
        }

        counterForIdNumber++;
        task.setIdNumber(counterForIdNumber);
        tasks.put(counterForIdNumber, task);
        return true;
    }

    public ArrayList<Task> getAllTasks() {
        if (tasks.isEmpty()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(tasks.values());
    }

    public boolean removeAllTasks() {
        if (tasks.isEmpty()) {
            return false;
        }

        tasks.clear();
        return true;
    }

    public Task getTaskForID(Integer taskIdNumber) {
        if (taskIdNumber == null || tasks.isEmpty()) {
            return (Task) buildObjectNull("Task");
        }

        if (tasks.containsKey(taskIdNumber)) {
            return tasks.get(taskIdNumber);
        } else {
            return (Task) buildObjectNull("Task");
        }
    }

    public boolean updateTask(Task task) {
        if (task == null || tasks.isEmpty()) {
            return false;
        }

        tasks.put(task.getIdNumber(), task);
        return true;
    }

    public boolean removeTaskForIdNumber(Integer taskIdNumber) {
        if (taskIdNumber == null || tasks.isEmpty()) {
            return false;
        }

        if (tasks.containsKey(taskIdNumber)) {
            tasks.remove(taskIdNumber);
            return true;
        } else {
            return false;
        }
    }

//EPIC
    public boolean addEpic(Epic epic) {
        if (epic == null) {
            return false;
        }

        counterForIdNumber++;
        epic.setIdNumber(counterForIdNumber);
        epics.put(counterForIdNumber, epic);
        return true;
    }

    public ArrayList<Epic> getAllEpics() {
        if (epics.isEmpty()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getSubtasksFromEpic(Integer epicID) {
        if (epicID == null || epics.isEmpty() || subtasks.isEmpty()) {
            return new ArrayList<>();
        }

        if (epics.containsKey(epicID)) {
            ArrayList<Subtask> allSubtasks = new ArrayList<>();
            allSubtasks.addAll(epics.get(epicID).getAllSubtasks().values());
            return allSubtasks;
        } else {
            return new ArrayList<>();
        }
    }

    public boolean updateEpic(Epic epic) {
        if (epic == null || epics.isEmpty()) {
            return false;
        }

        epics.put(epic.getIdNumber(), epic);
        return true;
    }

    public boolean removeAllEpics() {
        //true - если была очистка эпиков, в остальных случаях false
        if (epics.isEmpty()) {
            if (subtasks.isEmpty()) {
                return false;
            } else {
                subtasks.clear();
                return false;
            }
        } else {
            epics.clear();
            if (subtasks.isEmpty()) {
                return true;
            } else {
                subtasks.clear();
                return true;
            }
        }
    }

    public Epic getEpicForIdNumber(Integer epicIdNumber) {
        if (epicIdNumber == null || epics.isEmpty()) {
            return (Epic) buildObjectNull("Epic");
        }

        if (epics.containsKey(epicIdNumber)) {
            return epics.get(epicIdNumber);
        } else {
            return null;
        }
    }

    public boolean removeEpicForIdNumber(Integer epicIdNumber) {
        if (epicIdNumber == null || epics.isEmpty()) {
            return false;
        }

        if (epics.containsKey(epicIdNumber)) {
            if (subtasks.isEmpty()) {
                epics.remove(epicIdNumber);
            } else {
                for (Integer subtaskIdNumber : epics.get(epicIdNumber).getAllSubtasks().keySet()) {
                    subtasks.remove(subtaskIdNumber);//чистим список подзадач перед удалением эпика
                }
                epics.remove(epicIdNumber);
            }
            return true;
        } else {
            return false;
        }
    }

//SUBTASK
    public boolean addSubtask(Subtask subtask) {
        if (subtask == null || epics.isEmpty()) {
            return false;
        }

        if (epics.containsKey(subtask.getParentEpicIdNumber())) {
            counterForIdNumber++;
            subtask.setIdNumber(counterForIdNumber);
            epicForWork = epics.get(subtask.getParentEpicIdNumber());
            epicForWork.addSubtask(subtask);
            subtasks.put(subtask.getIdNumber(), subtask.getParentEpicIdNumber());
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<Subtask> getAllSubtasks() {
        if (epics.isEmpty()) {
            return new ArrayList<>();
        } else if (subtasks.isEmpty()) {
            return new ArrayList<>();
        }

        ArrayList<Subtask> allSubtasks = new ArrayList<>();

        for (Epic epic : epics.values()) {
            for (Subtask subtask : epic.getAllSubtasks().values()) {
                allSubtasks.add(subtask);
            }
        }

        return allSubtasks;
    }

    public boolean removeAllSubtasks() {
        if (epics.isEmpty()) {
            return false;
        } else if (subtasks.isEmpty()) {
            return false;
        }

        for (Epic epic : epics.values()) {
            epic.removeAllSubtasks();
        }

        subtasks.clear();
        return true;
    }

    public Subtask getSubtaskForIdNumber(Integer subtaskIdNumber) {
        if (subtaskIdNumber == null || epics.isEmpty() || subtasks.isEmpty()) {
            return (Subtask) buildObjectNull("Subtask");
        }

        if (subtasks.containsKey(subtaskIdNumber)) {
            Integer parentEpicIdNumber = subtasks.get(subtaskIdNumber);
            epicForWork = epics.get(parentEpicIdNumber);
            return epicForWork.getSubtaskForIdNumber(subtaskIdNumber);
        } else {
            return (Subtask) buildObjectNull("Subtask");
        }
    }

    public boolean removeSubtaskForIdNumber(Integer subtaskIdNumber) {
        if (subtaskIdNumber == null || epics.isEmpty() || subtasks.isEmpty()) {
            return false;
        }

        if (subtasks.containsKey(subtaskIdNumber)) {
            Integer parentEpicIdNumber = subtasks.get(subtaskIdNumber);
            if (epics.containsKey(parentEpicIdNumber)) {
                epicForWork = epics.get(parentEpicIdNumber);
                epicForWork.removeSubtaskForIdNumber(subtaskIdNumber);
                subtasks.remove(subtaskIdNumber);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean updateSubtask(Subtask subtask) {
        if (subtask == null || epics.isEmpty() || subtasks.isEmpty()) {
            return false;
        }

        if (subtasks.containsKey(subtask.getIdNumber())) {
            Integer parentEpicIdNumber = subtask.getParentEpicIdNumber();
            if (epics.containsKey(parentEpicIdNumber)) {
                epicForWork = epics.get(parentEpicIdNumber);
                epicForWork.updateSubtask(subtask);
                subtasks.put(subtask.getIdNumber(), subtask.getParentEpicIdNumber());
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private Object buildObjectNull(String objectType) {
        if (objectType == "Task") {
            Task taskNull = new Task("null", "null", StatusProgress.NULL);
            taskNull.setIdNumber(0);
            return taskNull;
        } else if (objectType == "Epic") {
            Epic epicNull = new Epic("null", "null");
            epicNull.setIdNumber(0);
            return epicNull;
        } else {
            Subtask subtaskNull = new Subtask(0, "null", "null", StatusProgress.NULL);
            subtaskNull.setIdNumber(0);
            return subtaskNull;
        }
    }
}
