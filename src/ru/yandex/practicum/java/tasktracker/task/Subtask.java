package ru.yandex.practicum.java.tasktracker.task;

import ru.yandex.practicum.java.tasktracker.manage.StatusProgress;

public class Subtask extends Task {
    private int parentEpicIdNumber;

    public Subtask(int parentEpicIdNumber, String name, String description, StatusProgress statusProgress) {
        super(name, description, statusProgress);
        this.parentEpicIdNumber = parentEpicIdNumber;
    }

    public int getParentEpicIdNumber() {
        return parentEpicIdNumber;
    }

    @Override
    public String toString() {
        String descriptionLength;
        if (getDescription() == null) {
            descriptionLength = "null";
        } else {
            descriptionLength = String.format("%d",getDescription().length());
        }

        return String.format("%nSubtask{name='%s', description.length='%s', Status Progress='%s', ID number='%d', PARENT_EPIC_ID number='%d'",
                getName(), descriptionLength, statusProgress.name(), getIdNumber(), parentEpicIdNumber);
    }
}
