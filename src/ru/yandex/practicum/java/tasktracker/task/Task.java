package ru.yandex.practicum.java.tasktracker.task;

import ru.yandex.practicum.java.tasktracker.manage.StatusProgress;
import java.util.Objects;

public class Task {
    private String name;
    private String description;
    public StatusProgress statusProgress;
    private int idNumber;

    public Task(String name, String description) {
        this.name = Objects.requireNonNull(name, "'name' can't be null");
        this.description = Objects.requireNonNull(description, "'description' can't be null");
    }

    public Task(String name, String description, StatusProgress statusProgress) {
        this(name, description);
        this.statusProgress = Objects.requireNonNull(statusProgress, "'statusProgress' can't be null");
    }

    public String getName() {
        return name;
    }

    public boolean setName(String newName) {
        if (newName == null) {
            return false;
        }

        name = newName;
        return true;
    }

    public String getDescription() {
        return description;
    }

    public boolean setDescription(String newDescription) {
        if (newDescription == null) {
            return false;
        }

        description = newDescription;
        return true;
    }

    public void setIdNumber(int idNumber) {
        this.idNumber = idNumber;
    }

    public int getIdNumber() {
        return idNumber;
    }

    public StatusProgress getStatusProgress() {
        return statusProgress;
    }

    @Override
    public String toString() {
        String descriptionLength;
        if (description == null) {
            descriptionLength = "null";
        } else {
            descriptionLength = String.format("%d",description.length());
        }

        return String.format("%nTask{name='%s', description.length='%s', Status Progress='%s', ID number='%d'",
                name, descriptionLength, statusProgress.name(), idNumber);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Task task = (Task) object;

        return Objects.equals(name, task.name)
                && Objects.equals(description, task.description)
                && Objects.equals(idNumber, task.idNumber)
                && Objects.equals(statusProgress, task.statusProgress);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(description);
        result = 31 * result + Objects.hashCode(statusProgress);
        result = 31 * result + Objects.hashCode(idNumber);

        return result;
    }
}
