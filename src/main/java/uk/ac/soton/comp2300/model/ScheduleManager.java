package uk.ac.soton.comp2300.model;

import javafx.collections.FXCollections;

import java.util.ArrayList;
import java.util.Comparator;
import javafx.collections.ObservableList;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

public class ScheduleManager {

    private static final ObservableList<ScheduleTask> tasks =
            FXCollections.observableArrayList();

    public static void loadFrom(List<ScheduleTask> tasks) { getTasks().setAll(tasks); }
    public static List<ScheduleTask> export() { return new ArrayList<>(getTasks()); }


    public static ObservableList<ScheduleTask> getTasks() {
        return tasks;
    }

    public static boolean addTask(ScheduleTask newTask) {
        for (ScheduleTask task : tasks) {
            if (task.getDeviceName().equalsIgnoreCase(newTask.getDeviceName())
                    && task.getTime().equals(newTask.getTime())) {
                return false;
            }
        }

        tasks.add(newTask);
        sortTasks();

        // Grant XP for the act of scheduling a device
        uk.ac.soton.comp2300.App.getInstance().addXp(50);

        // Calculate the next occurrence of this time
        LocalDateTime triggerTime = LocalDateTime.now()
                .withHour(newTask.getTime().getHour())
                .withMinute(newTask.getTime().getMinute())
                .withSecond(0)
                .withNano(0);

        // If the chosen time has already passed today, set it for tomorrow
        if (triggerTime.isBefore(LocalDateTime.now())) {
            triggerTime = triggerTime.plusDays(1);
        }

        // TEST MODE: Uncomment the line below to make notifications appear instantly for testing
        // triggerTime = LocalDateTime.now().minusSeconds(10);

        String fullMessage = newTask.getDescription() + " Reward: Money 10";

        uk.ac.soton.comp2300.model.Notification note = new uk.ac.soton.comp2300.model.Notification(
                uk.ac.soton.comp2300.model.Notification.Source.SCHEDULER,
                uk.ac.soton.comp2300.model.Notification.Type.REMINDER,
                newTask.getDeviceName(),
                fullMessage,
                triggerTime,
                triggerTime,
                "SCHED-" + java.util.UUID.randomUUID().toString()
        );

        // Add to central repository so the NotificationLogic loop picks it up
        uk.ac.soton.comp2300.App.getInstance().getRepository().add(note);

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