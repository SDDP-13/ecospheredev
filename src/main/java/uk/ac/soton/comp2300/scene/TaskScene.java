package uk.ac.soton.comp2300.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import uk.ac.soton.comp2300.model.Task;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;

import java.util.List;

public class TaskScene extends BaseScene {
    private List<Task> dailyTasks;

    public TaskScene(MainWindow mainWindow) { super(mainWindow); }

    @Override
    public void build() {
        var app = uk.ac.soton.comp2300.App.getInstance();
        this.dailyTasks = app.getTasks();

        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.getStyleClass().add("root-light");

        Button btnBack = new Button("←");
        btnBack.setPrefSize(44, 44);
        btnBack.getStyleClass().add("menu-icon-button");
        btnBack.setOnAction(e -> mainWindow.loadScene(new MenuScene(mainWindow)));
        StackPane.setAlignment(btnBack, Pos.TOP_LEFT);
        StackPane.setMargin(btnBack, new Insets(20));

        VBox container = new VBox(20);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(20, 20, 20, 20));

        Label title = new Label("Daily Tasks");
        title.getStyleClass().add("title-xlarge");

        Label windowDesc = new Label("Tasks will reset at 08:00 GMT");
        windowDesc.getStyleClass().add("title-medium");

        VBox taskList = new VBox(15);
        taskList.setAlignment(Pos.TOP_CENTER);

        ScrollPane taskScrollPane = new ScrollPane(taskList);
        taskScrollPane.setFitToWidth(true);
        taskScrollPane.setPrefViewportHeight(600);
        taskScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        for (Task task : dailyTasks) {
            taskList.getChildren().add(createTask(task));
        }

        container.getChildren().addAll(title, windowDesc, taskScrollPane);
        root.getChildren().addAll(container, btnBack);
    }

    private HBox createTask(Task taskObj) {
        var app = uk.ac.soton.comp2300.App.getInstance();
        HBox taskCard = new HBox(15);
        taskCard.getStyleClass().add("card");
        taskCard.setMaxWidth(400);
        taskCard.setAlignment(Pos.CENTER_LEFT);

        VBox textContainer = new VBox(5);
        textContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        Label title = new Label(taskObj.getId());
        title.getStyleClass().addAll("title-large-font");

        Label desc = new Label(taskObj.getDescription());
        desc.setWrapText(true);
        desc.setMaxWidth(250);
        desc.getStyleClass().add("label-small");

        Label rewardLabel = new Label("Rewards: Money 100, Wood 50, Metal 20");
        rewardLabel.getStyleClass().add("label-small");

        textContainer.getChildren().addAll(title, desc, rewardLabel);

        Button claimBtn = new Button();
        claimBtn.setMinWidth(100);

        var cookieStorage = uk.ac.soton.comp2300.App.getInstance().getCookieStorageService();
        int completedCount = app.getCompletedScheduledTasks();
        boolean isLocked = false;

        if (taskObj.getId().startsWith("Did a scheduled task (1)")) {
            if (completedCount < 1) { isLocked = true; setBtnLocked(claimBtn, "LOCKED (0/1)"); }
        } else if (taskObj.getId().startsWith("Did a scheduled task (2)")) {
            if (completedCount < 3) { isLocked = true; setBtnLocked(claimBtn, "LOCKED (" + completedCount + "/3)"); }
        } else if (taskObj.getId().startsWith("Build a structure (1)")) {
            int buildCount = app.getBuildingsPlacedCount();
            if (buildCount < 1) { isLocked = true; setBtnLocked(claimBtn, "LOCKED (0/1)"); }
        } else if (taskObj.getId().startsWith("Build a structure (2)")) {
            int buildCount = app.getBuildingsPlacedCount();
            if (buildCount < 5) { isLocked = true; setBtnLocked(claimBtn, "LOCKED (" + buildCount + "/5)"); }
        }
        else if (taskObj.getId().startsWith("Followed a recommenda")) {
            claimBtn.setDisable(true);
            claimBtn.setText("CHECKING...");

            Thread thread = new Thread(() -> {
                int recFollowedCount = checkHowManyRecommendationsFollowed();
                javafx.application.Platform.runLater(() -> {
                    boolean currentLockStatus = false;
                    if (taskObj.getDescription().contains("1 Device") && recFollowedCount < 1) currentLockStatus = true;
                    else if (taskObj.getDescription().contains("3 Devices") && recFollowedCount < 3) currentLockStatus = true;

                    if (currentLockStatus) {
                        String target = taskObj.getDescription().contains("1") ? "1" : "3";
                        setBtnLocked(claimBtn, "LOCKED (" + recFollowedCount + "/" + target + ")");
                    } else {
                        if (taskObj.getRewardCollected() || cookieStorage.hasClaimedTaskToday(taskObj.getId())) {
                            setBtnClaimed(claimBtn);
                        } else {
                            setBtnReady(claimBtn);
                        }
                    }
                });
            });
            thread.setDaemon(true);
            thread.start();
        }

        // --- Final State Check ---
        if (!isLocked && !taskObj.getId().startsWith("Followed a recommenda")) {
            if (taskObj.getRewardCollected() || cookieStorage.hasClaimedTaskToday(taskObj.getId())) {
                setBtnClaimed(claimBtn);
            } else {
                setBtnReady(claimBtn);
            }
        }

        claimBtn.setOnAction(e -> {
            if (taskObj.getRewardCollected() || cookieStorage.hasClaimedTaskToday(taskObj.getId())) {
                setBtnClaimed(claimBtn);
                return;
            }
            taskObj.toggleRewardCollected();
            app.addTaskCompletion();
            var controller = app.getGameController();
            app.addXp(100);
            for (var stack : taskObj.getRewards()) {
                controller.addResource(stack.getType(), stack.getAmount());
            }
            cookieStorage.markTaskClaimedToday(taskObj.getId());
            setBtnClaimed(claimBtn);
        });

        taskCard.getChildren().addAll(textContainer, claimBtn);
        return taskCard;
    }
    private int checkHowManyRecommendationsFollowed() {
        var schedules = uk.ac.soton.comp2300.model.ScheduleManager.getTasks();
        if (schedules.isEmpty()) return 0;

        // 1. Initialize service once outside the loop
        var recService = new uk.ac.soton.comp2300.recommendation_logic.RecommendationService();
        int count = 0;

        // 2. Optimization: Use a Set to store unique device names to avoid duplicate API calls
        java.util.Set<String> uniqueDevices = new java.util.HashSet<>();
        for (var s : schedules) uniqueDevices.add(s.getDeviceName());

        // 3. Map device names to their recommended hours
        java.util.Map<String, Integer> recommendedHours = new java.util.HashMap<>();
        for (String device : uniqueDevices) {
            String recommendation = recService.getRecommendation(device);
            try {
                String timePart = recommendation.split("Best time is ")[1].split(" -")[0];
                int recHour = java.time.LocalTime.parse(timePart).getHour();
                recommendedHours.put(device, recHour);
            } catch (Exception ignored) {}
        }

        // 4. Compare schedules against the pre-fetched mapped hours
        for (var schedule : schedules) {
            Integer recHour = recommendedHours.get(schedule.getDeviceName());
            if (recHour != null && schedule.getTime().getHour() == recHour) {
                count++;
            }
        }
        return count;
    }

    private void setBtnLocked(Button btn, String text) {
        btn.setText(text);
        btn.setDisable(true);
        btn.getStyleClass().addAll("button secondary", "button-shape-rounded-large", "border color");
    }

    private void setBtnClaimed(Button btn) {
        btn.setText("CLAIMED");
        btn.setDisable(true);
        btn.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #999; -fx-font-size: 13px; -fx-background-radius: 20;");
    }

    private void setBtnReady(Button btn) {
        btn.setText("CLAIM");
        btn.setDisable(false);
        btn.getStyleClass().add("button-claim");
    }

    @Override public void initialise() {}
}