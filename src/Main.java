public class Main {

    public static void main(String[] args) {
        //простой тест из тз
        TaskManager taskManager = new TaskManager();

        Task task1 = new Task("first", "first task", StatusProgress.NEW);
        Task task2 = new Task("second", "second task", StatusProgress.NEW);
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        Epic epic1 = new Epic("epic-1", "this epic with 1 sub");
        Epic epic2 = new Epic("epic-2", "this epic with 2 sub");

        taskManager.addEpic(epic1);//1sub
        taskManager.addEpic(epic2);//2sub

        Subtask subtask1 = new Subtask(epic1.getID(), "1-1", "1 sub", StatusProgress.NEW);
        Subtask subtask2 = new Subtask(epic2.getID(), "2-2", "2 sub", StatusProgress.NEW);
        Subtask subtask3 = new Subtask(epic2.getID(), "3-2", "3 sub", StatusProgress.NEW);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);

        //print
        System.out.println("TASKs");
        System.out.println(taskManager.getAllTasks());
        System.out.println();
        System.out.println("EPICs");
        System.out.println(taskManager.getAllEpics());
        System.out.println();
        System.out.println("SUBTASKs");
        System.out.println(taskManager.getAllSubtasks());
        System.out.println();

        System.out.println("TASKs change StatusProgress");
        task1.statusProgress = StatusProgress.IN_PROGRESS;
        task2.statusProgress = StatusProgress.DONE;
        taskManager.updateTask(task1);
        taskManager.updateTask(task2);
        System.out.println("SUBTASKs change StatusProgress");
        subtask1.statusProgress = StatusProgress.IN_PROGRESS;
        subtask2.statusProgress = StatusProgress.DONE;
        subtask3.statusProgress = StatusProgress.IN_PROGRESS;
        taskManager.updateSubtask(subtask1);
        taskManager.updateSubtask(subtask2);
        taskManager.updateSubtask(subtask3);
        System.out.println();

        //print
        System.out.println("TASKs");
        System.out.println(taskManager.getAllTasks());
        System.out.println();
        System.out.println("EPICs");
        System.out.println(taskManager.getAllEpics());
        System.out.println();
        System.out.println("SUBTASKs");
        System.out.println(taskManager.getAllSubtasks());
        System.out.println();

        System.out.println("TASK 2 Удаление по идентификатору.");
        taskManager.removeTaskForID(task2.getID());
        System.out.println("EPIC 1 Удаление по идентификатору.");
        taskManager.removeEpicForID(epic1.getID());
        System.out.println("SUBTASK 3 Удаление по идентификатору.");
        taskManager.removeSubtaskForID(subtask3.getID());

        //print
        System.out.println("TASKs");
        System.out.println(taskManager.getAllTasks());
        System.out.println();
        System.out.println("EPICs");
        System.out.println(taskManager.getAllEpics());
        System.out.println();
        System.out.println("SUBTASKs");
        System.out.println(taskManager.getAllSubtasks());
        System.out.println();
    }
}
