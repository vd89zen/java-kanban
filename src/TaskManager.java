import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private static int counterForID;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, Integer> subtasks; // (ключ)ID подзадачи-(значение)ID её родителя
    private static Epic epicForWork;

    public TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        counterForID = 0;
    }

//TASK
    public void addTask(Task task) {
        if (task == null) {
            return;
        }

        counterForID++;
        task.setID(counterForID);
        tasks.put(counterForID, task);
    }

    public ArrayList<Task> getAllTasks() {
        if (tasks.isEmpty()) {
            return null;
        }

        return new ArrayList<>(tasks.values());
    }

    public void removeAllTasks() {
        if (tasks.isEmpty()) {
            return;
        }

        tasks.clear();
    }

    public Task getTaskForID(Integer taskID) {
        if (taskID == null || tasks.isEmpty()) {
            return null;
        }

        if (tasks.containsKey(taskID)) {
            return tasks.get(taskID);
        } else {
            return null;
        }
    }

    public void updateTask(Task task) {
        if (task == null || tasks.isEmpty()) {
            return;
        }

        tasks.put(task.getID(), task);
    }

    public void removeTaskForID(Integer taskID) {
        if (taskID == null || tasks.isEmpty()) {
            return;
        }

        if (tasks.containsKey(taskID)) {
            tasks.remove(taskID);
        }
    }

//EPIC
    public void addEpic(Epic epic) {
        if (epic == null) {
            return;
        }

        counterForID++;
        epic.setID(counterForID);
        epics.put(counterForID, epic);
    }

    public ArrayList<Epic> getAllEpics() {
        if (epics.isEmpty()) {
            return null;
        }

        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getSubtasksFromEpic(Integer epicID) {
        if (epicID == null || epics.isEmpty() || subtasks.isEmpty()) {
            return null;
        }

        if (epics.containsKey(epicID)) {
            ArrayList<Subtask> allSubtasks = new ArrayList<>();
            for (Subtask subtask : epics.get(epicID).getAllSubtasks().values()) {
                allSubtasks.add(subtask);
            }

            return allSubtasks;
        } else {
            return null;
        }
    }

    public void updateEpic(Epic epic) {
        if (epic == null || epics.isEmpty()) {
            return;
        }

        epics.put(epic.getID(), epic);
    }

    public void removeAllEpics() {
        if (epics.isEmpty()) {
            return;
        } else {
            epics.clear();
        }

        if (subtasks.isEmpty()) {
            return;
        } else {
            subtasks.clear();
        }
    }

    public Epic getEpicForID(Integer epicID) {
        if (epicID == null || epics.isEmpty()) {
            return null;
        }

        if (epics.containsKey(epicID)) {
            return epics.get(epicID);
        } else {
            return null;
        }
    }

    public void removeEpicForID(Integer epicID) {
        if (epicID == null || epics.isEmpty()) {
            return;
        }

        if (epics.containsKey(epicID)) {
            if (subtasks.isEmpty()) {
                epics.remove(epicID);
            } else {
                for (Integer subtaskID : epics.get(epicID).getAllSubtasks().keySet()) {
                    subtasks.remove(subtaskID);//чистим список подзадач перед удалением эпика
                }
                epics.remove(epicID);
            }
        }
    }

//SUBTASK
    public void addSubtask(Subtask subtask) {
        if (subtask == null || epics.isEmpty()) {
            return;
        }

        counterForID++;
        subtask.setID(counterForID);
        epicForWork = epics.get(subtask.getParentEpicID());
        epicForWork.addSubtask(subtask);
        subtasks.put(subtask.getID(), subtask.getParentEpicID());
    }

    public ArrayList<Subtask> getAllSubtasks() {
        if (epics.isEmpty()) {
            return null;
        } else if (subtasks.isEmpty()) {
            return null;
        }

        ArrayList<Subtask> allSubtasks = new ArrayList<>();

        for (Epic epic : epics.values()) {
            for (Subtask subtask : epic.getAllSubtasks().values()) {
                allSubtasks.add(subtask);
            }
        }

        return allSubtasks;
    }

    public void removeAllSubtasks() {
        if (epics.isEmpty()) {
            return;
        } else if (subtasks.isEmpty()) {
            return;
        }

        for (Epic epic : epics.values()) {
            epic.removeAllSubtasks();
        }

        subtasks.clear();
    }

    public Subtask getSubtaskForID(Integer subtaskID) {
        if (subtaskID == null || epics.isEmpty() || subtasks.isEmpty()) {
            return null;
        }

        if (subtasks.containsKey(subtaskID)) {
            //return epics.get(subtasks.get(subtaskID)).getSubtaskForID(subtaskID); - вообще, можно в одну строку было, но читать сложнее
            Integer parentEpicID = subtasks.get(subtaskID);
            epicForWork = epics.get(parentEpicID);

            return epicForWork.getSubtaskForID(subtaskID);
        } else {
            return null;
        }
    }

    public void removeSubtaskForID(Integer subtaskID) {
        if (subtaskID == null || epics.isEmpty() || subtasks.isEmpty()) {
            return;
        }

        if (subtasks.containsKey(subtaskID)) {
            //epics.get(subtasks.get(subtaskID)).removeSubtaskForID(subtaskID); - и тут можно в одну строку было.
            Integer parentEpicID = subtasks.get(subtaskID);
            epicForWork = epics.get(parentEpicID);
            epicForWork.removeSubtaskForID(subtaskID);
            subtasks.remove(subtaskID);
        }
    }

    public void updateSubtask(Subtask subtask) {
        if (subtask == null || epics.isEmpty() || subtasks.isEmpty()) {
            return;
        }

        if (subtasks.containsKey(subtask.getID())) {
            epicForWork = epics.get(subtask.getParentEpicID());
            epicForWork.updateSubtask(subtask);
            subtasks.put(subtask.getID(), subtask.getParentEpicID());
        }
    }

}
