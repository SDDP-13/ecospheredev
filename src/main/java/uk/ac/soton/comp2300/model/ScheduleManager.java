package uk.ac.soton.comp2300.model;

import javafx.collections.FXCollections;
import java.util.Comparator;
import javafx.collections.ObservableList;
import java.time.LocalTime;


public class ScheduleManager {

    private static final ObservableList<ScheduleTask> tasks =
            FXCollections.observableArrayList();

    public static ObservableList<ScheduleTask> getTasks() {
        return tasks;
    }

    public static boolean addTask(ScheduleTask newTask) {
    for (ScheduleTask task : tasks) {
        if (task.getDeviceName().equalsIgnoreCase(newTask.getDeviceName())
                && task.getTime().equals(newTask.getTime())) {
            return false; // The same task already exists at the desired time
        }
    }
    tasks.add(newTask);
    sortTasks();
    return true;
}

    public static boolean updateTask(ScheduleTask taskToEdit, String newDevice, LocalTime newTime, String newDescription) {
    for (ScheduleTask t : tasks) {
        if (t != taskToEdit &&
            t.getDeviceName().equalsIgnoreCase(newDevice) &&
            t.getTime().equals(newTime)) {
            return false;
        }
    }
    taskToEdit.setDeviceName(newDevice);
    taskToEdit.setTime(newTime);
    taskToEdit.setDescription(newDescription);
    sortTasks();
    return true;
}

    public static void removeTask(ScheduleTask task) {
        tasks.remove(task);
    }

    private static void sortTasks() {
    FXCollections.sort(tasks, Comparator.comparing(ScheduleTask::getTime));
}
}