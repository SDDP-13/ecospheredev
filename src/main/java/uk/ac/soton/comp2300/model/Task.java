package uk.ac.soton.comp2300.model;

import java.util.List;

public class Task {
    private final String id;
    private final String description;
    private final List<ResourceStack> rewards;
    private Boolean completed = false;
    private Boolean rewardCollected = false;

    public Task(String id, String description, List<ResourceStack> rewards) {
        this.id = id;
        this.description = description;
        this.rewards = rewards;
    }

    public String getId() { return id; }
    public String getDescription() { return description; }
    public List<ResourceStack> getRewards() { return rewards; }
    public Boolean getCompleted() { return completed; }
    public Boolean getRewardCollected() { return rewardCollected; }
    public void toggleRewardCollected() { rewardCollected = !rewardCollected; }
    public void toggleCompleted() { completed = !completed; }

    @Override
    public String toString() {
        return id + ": " + description + " | Rewards: " + rewards;
    }
}
