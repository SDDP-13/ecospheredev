package uk.ac.soton.comp2300.model;

import java.util.ArrayList;
import java.util.List;

public class TaskPool {
    private List<Task> allTasks;

    /**
     * This constructor handles the case where you want to pass a list in.
     * Fixes: 'TaskPool()' cannot be applied to '(java.util.List...)'
     */
    public TaskPool(List<Task> tasks) {
        this.allTasks = tasks != null ? tasks : new ArrayList<>();
    }

    /**
     * This constructor handles the 'new TaskPool()' case for the App class.
     */
    public TaskPool() {
        this.allTasks = TaskLoader.loadTasks();
    }

    /**
     * This method provides the tasks to your Scene.
     * Fixes: Cannot resolve method 'generateDailyTasks'
     */
    public List<Task> generateDailyTasks() {
        // We return a copy of the list in the order defined in the JSON
        return new ArrayList<>(allTasks);
    }
}