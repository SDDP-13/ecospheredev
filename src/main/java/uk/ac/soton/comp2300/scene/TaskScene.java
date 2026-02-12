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
import uk.ac.soton.comp2300.model.TaskLoader;
import uk.ac.soton.comp2300.model.TaskPool;

import java.util.List;

public class TaskScene extends BaseScene {
    private TaskPool taskPool = new TaskPool(TaskLoader.loadTasks());
    private List<Task> dailyTasks = taskPool.generateDailyTasks();

    public TaskScene(MainWindow mainWindow) { super(mainWindow); }

    @Override
    public void build() {
        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.setStyle("-fx-background-color: #EFEEF5;"); // Light background from mockup

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
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: 800; -fx-text-fill: #333;");

        Label windowDesc = new Label("Tasks will reset at 08:00 GMT");
        windowDesc.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");

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
        HBox taskCard = new HBox(10);
        taskCard.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 12;");
        taskCard.setMaxWidth(400);
        taskCard.setAlignment(Pos.CENTER);

        VBox textContainer = new VBox(5);
        textContainer.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(taskObj.getId());
        title.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label desc = new Label(taskObj.getDescription());
        desc.setStyle("-fx-text-fill: #555; -fx-font-size: 14px;");

        // Smaller Rewards label
        Label rewardLabel = new Label("Rewards: " + taskObj.getRewards().toString());
        rewardLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px; -fx-font-weight: bold;");

        textContainer.getChildren().addAll(title, desc, rewardLabel);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Button claimBtn = new Button();
        // Setting fixed sizes to prevent the (...) truncation
        claimBtn.setMinWidth(60);
        claimBtn.setMinHeight(60);
        claimBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 28px; -fx-cursor: hand;");

        // Using standard Emojis which are more likely to be supported by your OS font
        if (taskObj.getRewardCollected()) {
            claimBtn.setText("⭐"); // Solid Gold Star
            claimBtn.setDisable(true);
            claimBtn.setOpacity(0.8);
        } else {
            claimBtn.setText("🔘"); // Using a circle/target emoji to represent the circled star
        }

        claimBtn.setOnAction(e -> {
            if (taskObj.getRewardCollected()) return;

            taskObj.toggleRewardCollected();
            var app = uk.ac.soton.comp2300.App.getInstance();

            for (uk.ac.soton.comp2300.model.ResourceStack stack : taskObj.getRewards()) {
                app.addResources(stack.getType(), stack.getAmount());
            }

            claimBtn.setDisable(true);
            claimBtn.setText("⭐");
        });

        taskCard.getChildren().addAll(textContainer, space, claimBtn);
        return taskCard;
    }

    @Override public void initialise() {}
}