package uk.ac.soton.comp2300.model;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/*
Loads tasks from JSON file into a list of Task objects
 */

public class TaskLoader {
    private static final String TASKS_PATH = "/assets/all_tasks.json";
    public static List<Task> loadTasks() {
        Gson gson = new Gson();     // Using GSON library to help with creating objects out of JSON file entries

        InputStream stream = TaskLoader.class.getResourceAsStream(TASKS_PATH);     // Turns JSON file into stream of bytes
        if (stream == null) {   // Handles missing JSON files
            throw new IllegalStateException(TASKS_PATH + " not found");
        }

        TaskFileData fileData = gson.fromJson(
                new InputStreamReader(stream),      // Turns byte stream into String
                TaskFileData.class      // fromJson follows structure of TaskFileData DTO class to correctly create TaskFileData objects
        );

        List<Task> tasks = new ArrayList<>();       // Turns file data into List of Task objects
        for (TaskData data : fileData.tasks) {
            List<ResourceStack> rewards = new ArrayList<>();
            for (RewardData reward : data.rewards) {    // Iterates through rewards list - allows tasks to give multiple rewards
                Resource.fromString(reward.type)
                        .map(type -> new ResourceStack(type, reward.amount))
                        .ifPresent(rewards::add);       // Makes use of Optional wrapper to turn Resource enums into ResourceStack objects and add them to the list
            }
            tasks.add(new Task(
                    data.id,
                    data.description,
                    rewards
            ));
        }

        return tasks;
    }
}
