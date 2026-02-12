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
        // -----------------------

        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.setStyle("-fx-background-color: #EFEEF5;");

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
        HBox taskCard = new HBox(15);
        taskCard.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 12;");
        taskCard.setMaxWidth(400);
        taskCard.setAlignment(Pos.CENTER_LEFT);

        VBox textContainer = new VBox(5);
        textContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContainer, Priority.ALWAYS);

        Label title = new Label(taskObj.getId());
        title.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label desc = new Label(taskObj.getDescription());
        desc.setWrapText(true);
        desc.setMaxWidth(250);
        desc.setStyle("-fx-text-fill: #555; -fx-font-size: 13px;");

        Label rewardLabel = new Label("Rewards: Money 100, Wood 50, Metal 20");
        rewardLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px; -fx-font-weight: bold;");

        textContainer.getChildren().addAll(title, desc, rewardLabel);

        Button claimBtn = new Button();
        claimBtn.setMinWidth(100);

        if (taskObj.getRewardCollected()) {
            setBtnClaimed(claimBtn);
        } else {
            setBtnReady(claimBtn);
        }

        claimBtn.setOnAction(e -> {
            if (taskObj.getRewardCollected()) return;

            taskObj.toggleRewardCollected(); // Updates the object in the App's list

            var app = uk.ac.soton.comp2300.App.getInstance();
            for (var stack : taskObj.getRewards()) {
                app.addResources(stack.getType(), stack.getAmount());
            }

            setBtnClaimed(claimBtn);
        });

        taskCard.getChildren().addAll(textContainer, claimBtn);
        return taskCard;
    }

    private void setBtnClaimed(Button btn) {
        btn.setText("CLAIMED");
        btn.setDisable(true);
        btn.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #999; -fx-font-size: 13px; -fx-background-radius: 20;");
    }

    private void setBtnReady(Button btn) {
        btn.setText("CLAIM");
        btn.setDisable(false);
        btn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 20; -fx-cursor: hand;");
    }

    @Override public void initialise() {}
}