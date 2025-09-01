package ru.yandex.practicum.java.tasktracker.utils;

import java.util.Objects;

public abstract class AbstractTask {
    protected Integer idNumber;
    protected String name;
    protected String description;
    protected StatusProgress statusProgress;
    protected TypesTasks type;

    //возможность создания пустышки; в том числе для случаев, когда объект не найден
    protected AbstractTask() {
        this.idNumber = null;
        this.name = null;
        this.description = null;
        this.statusProgress = null;
    }

    //возможность создания копии
    protected AbstractTask(AbstractTask task) {
        this.idNumber = task.idNumber;
        this.name = task.name;
        this.description = task.description;
        this.statusProgress = task.statusProgress;
        this.type = task.type;
    }

    protected AbstractTask(String name, String description, StatusProgress statusProgress) {
        idNumber = 0;
        this.name = Objects.requireNonNull(name, "'name' can't be null");
        this.description = Objects.requireNonNull(description, "'description' can't be null");
        this.statusProgress = Objects.requireNonNull(statusProgress, "'statusProgress' can't be null");
    }

    protected AbstractTask(Integer idNumber, String name, String description, StatusProgress statusProgress) {
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
