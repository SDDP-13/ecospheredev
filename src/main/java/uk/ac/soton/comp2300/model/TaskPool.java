package uk.ac.soton.comp2300.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/*
Class which holds the serialised tasks and has method for random daily task creation
 */

public class TaskPool {
    private final List<Task> allTasks;


    public TaskPool(List<Task> allTasks) {
        this.allTasks = allTasks;
    }

    public List<Task> getAllTasks() { return allTasks; }

    public Optional<Task> getById(String id) {
        return allTasks.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();
    }

    public List<Task> generateDailyTasks() {
        List<Task> copy = new ArrayList<>(allTasks);
        int dailyTaskCount = 5;

        if (this.allTasks.size() <= dailyTaskCount) { return allTasks; }
        else {
            Collections.shuffle(copy);
            return copy.subList(0, dailyTaskCount);
        }
    }
}
