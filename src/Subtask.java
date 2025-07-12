public class Subtask extends Task {
    private final int parentEpicID;

    public Subtask(int parentEpicID, String name, String description, StatusProgress statusProgress) {
        super(name, description, statusProgress);
        this.parentEpicID = parentEpicID;
    }

    public int getParentEpicID() {
        return parentEpicID;
    }

    @Override
    public String toString() {
        String descriptionLength;
        if (getDescription() == null) {
            descriptionLength = "null";
        } else {
            descriptionLength = String.format("%d",getDescription().length());
        }

        return String.format("%nSubtask{name='%s', description.length='%s', StatusProgress='%s', ID='%d', parentEpicID='%d'",
                getName(), descriptionLength, statusProgress.name(), getID(), parentEpicID);
    }
}
