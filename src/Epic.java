import java.util.HashMap;

public class Epic extends Task {
    private HashMap<Integer, Subtask> subtasks;
    private StatusProgress statusProgress;

    public Epic(String name, String description) {
        super(name, description);
        subtasks = new HashMap<>();
        updateStatusProgress();
    }

    protected void addSubtask(Subtask subtask) {
        subtasks.put(subtask.getID(), subtask);
        updateStatusProgress();
    }

    //метод идентичен addSubtask - но если надо будет изменить логику обновления, так будет проще
    protected void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getID(), subtask);
        updateStatusProgress();
    }

    protected Subtask getSubtaskForID(Integer subtaskID) {
        return subtasks.get(subtaskID);
    }

    protected void removeSubtaskForID(Integer subtaskID) {
        subtasks.remove(subtaskID);
        updateStatusProgress();
    }

    protected void removeAllSubtasks() {
        subtasks.clear();
        updateStatusProgress();
    }

    protected HashMap<Integer, Subtask> getAllSubtasks() {
        return subtasks;
    }

    private void updateStatusProgress() {
        if (subtasks.isEmpty()) {
            statusProgress = StatusProgress.NEW;
            return;
        }

        boolean epicDONE = true;
        boolean epicNEW = true;

        for (Subtask subtask : subtasks.values()) {
            StatusProgress statusSubtask = subtask.statusProgress;
            if (epicDONE) {
                if (statusSubtask == StatusProgress.IN_PROGRESS) {
                    statusProgress = StatusProgress.IN_PROGRESS;
                    return;
                } else if (statusSubtask == StatusProgress.NEW) {
                    epicDONE = false;
                }
            }

            if (epicNEW) {
                if (statusSubtask == StatusProgress.IN_PROGRESS) {
                    statusProgress = StatusProgress.IN_PROGRESS;
                    return;
                } else if (statusSubtask == StatusProgress.DONE) {
                    epicNEW = false;
                }
            }
        }

        if (epicDONE) {
            statusProgress = StatusProgress.DONE;
        } else if (epicNEW) {
            statusProgress = StatusProgress.NEW;
        } else {
            statusProgress = StatusProgress.IN_PROGRESS;
        }
    }

    @Override
    public StatusProgress getStatusProgress() {
        return statusProgress;
    }

    @Override
    public String toString() {
        String descriptionLength;
        if (getDescription() == null) {
            descriptionLength = "null";
        } else {
            descriptionLength = String.format("%d",getDescription().length());
        }

        String result  = String.format("%nEpic{name='%s', description.length='%s', StatusProgress='%s', ID='%d', number of subtusks='%d'",
                getName(), descriptionLength, statusProgress.name(), getID(), subtasks.size());
        return result;
    }
}
