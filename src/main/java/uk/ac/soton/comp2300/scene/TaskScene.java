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
        root.setStyle("-fx-background-color: #F6F3FB;");

        Button btnBack = new Button("⬅");
        btnBack.setPrefSize(44, 44);
        btnBack.getStyleClass().add("menu-icon-button");
        btnBack.setOnAction(e -> mainWindow.loadScene(new MenuScene(mainWindow)));
        StackPane.setAlignment(btnBack, Pos.TOP_LEFT);
        StackPane.setMargin(btnBack, new Insets(20));

        VBox container = new VBox();
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(20));
        container.setSpacing(20);

        Label title = new Label("Daily Tasks");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: 800;");


        Label windowDesc = new Label("Tasks will reset at 08:00 GMT");
        windowDesc.setStyle("-fx-font-size: 16px; -fx-font-weight: 400; -fx-text-fill: rgba(0, 0, 0, 0.8);");


        VBox taskList = new VBox(15);
        taskList.setAlignment(Pos.TOP_CENTER);

        ScrollPane taskScrollPane = new ScrollPane(taskList);
        taskScrollPane.setFitToWidth(true);
        taskScrollPane.setPrefViewportHeight(620);
        taskScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        taskScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        taskScrollPane.setStyle("-fx-background-color: #F6F3FB;");


        for (Task task : dailyTasks) {
            taskList.getChildren().add(createTask(task));
        }

        container.getChildren().addAll(title, windowDesc, taskScrollPane);
        root.getChildren().addAll(container, btnBack);


/*        VBox taskCard = new VBox(10);
        taskCard.setStyle("-fx-background-color: #333; -fx-padding: 15; -fx-background-radius: 10;");
        taskCard.setMaxWidth(300);

        Label taskTitle = new Label("Fix Solar Array");
        taskTitle.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        Label taskDesc = new Label("Efficiency is down by 15%. Click to repair.");
        taskDesc.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        taskCard.getChildren().addAll(taskTitle, taskDesc);
        container.getChildren().add(taskCard);*/
    }

    private HBox createTask(Task taskObj) {
        HBox taskCard = new HBox(10);
        taskCard.setStyle("-fx-background-color: #333; -fx-padding: 15; -fx-background-radius: 10;");
        taskCard.setMaxWidth(400);
        taskCard.setAlignment(Pos.CENTER);

        VBox textContainer = new VBox(5);
        textContainer.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(taskObj.getId());
        title.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label desc = new Label(taskObj.getDescription());
        desc.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        Label reward = new Label("Rewards: " + taskObj.getRewards().toString());
        reward.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        textContainer.getChildren().addAll(title, desc, reward);

        Button claimBtn = new Button("⭐");
        claimBtn.setPrefSize(70, 70);
        claimBtn.getStyleClass().add("menu-icon-button");
        claimBtn.setStyle("-fx-background-color: #F6F3FB; -fx-text-fill: black; -fx-font-size: 30px;");
        claimBtn.setAlignment(Pos.CENTER_RIGHT);
        claimBtn.setOnAction(e -> {
            taskObj.toggleRewardCollected();
            claimBtn.setDisable(true);
            claimBtn.setText("\uD83C\uDF1F");
            System.out.println("Reward collected");
        });

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        taskCard.getChildren().addAll(textContainer, space, claimBtn);

        return taskCard;
    }

    @Override public void initialise() {}
}