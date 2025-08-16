package ru.yandex.practicum.java.tasktracker.task;

import ru.yandex.practicum.java.tasktracker.manage.ResultOfOperation;
import java.util.Objects;

public abstract class AbstractTask {
    protected String name;
    protected String description;
    protected StatusProgress statusProgress;
    protected Integer idNumber;

    //возможность создания пустышки; в том числе для случаев, когда объект не найден
    protected AbstractTask() {
        this.name = null;
        this.description = null;
        this.statusProgress = null;
        this.idNumber = null;
    }

    //возможность создания копии
    protected AbstractTask(AbstractTask task) {
        this.name = task.name;
        this.description = task.description;
        this.statusProgress = task.statusProgress;
        this.idNumber = task.idNumber;
    }

    protected AbstractTask(String name, String description, StatusProgress statusProgress) {
        this.name = Objects.requireNonNull(name, "'name' can't be null");
        this.description = Objects.requireNonNull(description, "'description' can't be null");
        this.statusProgress = Objects.requireNonNull(statusProgress, "'statusProgress' can't be null");
        idNumber = 0;
    }

    protected AbstractTask(String name, String description, StatusProgress statusProgress, Integer idNumber) {
        this.name = Objects.requireNonNull(name, "'name' can't be null");
        this.description = Objects.requireNonNull(description, "'description' can't be null");
        this.statusProgress = Objects.requireNonNull(statusProgress, "'statusProgress' can't be null");
        if (idNumber < 0) {
            throw new IllegalArgumentException("'idNumber' can't be less zero");
        } else {
            this.idNumber = Objects.requireNonNull(idNumber, "'idNumber' can't be null");
        }
    }

    public String getName() {
        return name;
    }

    public ResultOfOperation setName(String newName) {
        if (newName == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        }

        name = newName;
        return ResultOfOperation.SUCCESS;
    }

    public String getDescription() {
        return description;
    }

    public ResultOfOperation setDescription(String newDescription) {
        if (newDescription == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        }

        description = newDescription;
        return ResultOfOperation.SUCCESS;
    }

    public ResultOfOperation setIdNumber(Integer idNumber) {
        if (idNumber == null) {
            return ResultOfOperation.ERROR_OBJECT_NULL;
        } else if (idNumber < 0) {
            return ResultOfOperation.ERROR_ID_LESS_ZERO;
        }

        this.idNumber = idNumber;
        return ResultOfOperation.SUCCESS;
    }

    public Integer getIdNumber() {
        return idNumber;
    }

    public StatusProgress getStatusProgress() {
        return statusProgress;
    }

}
