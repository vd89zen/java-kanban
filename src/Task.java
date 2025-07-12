import java.util.Objects;

public class Task {
    private String name;
    private String description;
    public StatusProgress statusProgress;
    private int ID;

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Task(String name, String description, StatusProgress statusProgress) {
        this(name, description);
        this.statusProgress = statusProgress;
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        if (newName == null) {
            return;
        }

        name = newName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String newDescription) {
        if (newDescription == null) {
            return;
        }

        description = newDescription;
    }

    protected void setID(int ID) {
        this.ID = ID;
    }

    public int getID() {
        return ID;
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

        return String.format("%nTask{name='%s', description.length='%s', StatusProgress='%s', ID='%d'",
                name, descriptionLength, statusProgress.name(), ID);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Task task = (Task) object;

        return Objects.equals(name, task.name)
                && Objects.equals(description, task.description)
                && Objects.equals(ID, task.ID)
                && Objects.equals(statusProgress, task.statusProgress);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(description);
        result = 31 * result + Objects.hashCode(statusProgress);
        result = 31 * result + Objects.hashCode(ID);

        return result;
    }



}
