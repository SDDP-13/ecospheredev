package uk.ac.soton.comp2300.scene;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import uk.ac.soton.comp2300.event.NotificationListenerInterface;
import uk.ac.soton.comp2300.event.NotificationRecord;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;
import java.time.format.DateTimeFormatter;

public class NotificationScene extends BaseScene implements NotificationListenerInterface {
    private VBox notificationList;

    public NotificationScene(MainWindow mainWindow) { super(mainWindow); }

    @Override
    public void build() {
        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.setStyle("-fx-background-color: #F4F0FF;");

        StackPane header = new StackPane();
        header.setPadding(new Insets(20, 10, 20, 10));
        header.setPrefWidth(mainWindow.getWidth());

        Button btnBack = new Button("←");
        btnBack.setStyle("-fx-background-color: transparent; -fx-font-size: 28px; -fx-cursor: hand;");
        btnBack.setOnAction(e -> mainWindow.loadScene(new MenuScene(mainWindow)));
        StackPane.setAlignment(btnBack, Pos.CENTER_LEFT);

        Label title = new Label("Notifications");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #333;");
        StackPane.setAlignment(title, Pos.CENTER);

        header.getChildren().addAll(title, btnBack);

        notificationList = new VBox(15);
        notificationList.setAlignment(Pos.TOP_CENTER);
        notificationList.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(notificationList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

        VBox mainLayout = new VBox(header, scrollPane);
        root.getChildren().add(mainLayout);

        // SYNC LOGIC: Fixed to match the 5-parameter NotificationRecord constructor
        var repo = uk.ac.soton.comp2300.App.getInstance().getRepository();
        for (var note : repo.getAllNotifications()) {
            if (note.getStatus() == uk.ac.soton.comp2300.model.Notification.Status.SENT) {
                notificationList.getChildren().add(0, createNotificationCard(new NotificationRecord(
                        note.getId(),             // Param 1: String id
                        note.getTitle(),          // Param 2: String title
                        note.getMessage(),        // Param 3: String message
                        note.getScheduled_Time(), // Param 4: LocalDateTime time
                        note.getType()            // Param 5: Notification.Type type
                )));
            }
        }
    }

    @Override
    public void onNotificationSent(NotificationRecord record) {
        Platform.runLater(() -> {
            notificationList.getChildren().add(0, createNotificationCard(record));
        });
    }

    private VBox createNotificationCard(NotificationRecord record) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: white; -fx-border-color: #D1C4E9; -fx-border-radius: 15; -fx-background-radius: 15; -fx-padding: 15;");
        card.setMaxWidth(400);

        HBox mainLayout = new HBox(15);
        mainLayout.setAlignment(Pos.CENTER_LEFT);

        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(60, 60);
        iconContainer.setStyle("-fx-background-color: #E6E0F8; -fx-background-radius: 12; -fx-border-color: #333; -fx-border-radius: 12;");
        Label icon = new Label(getIconForDevice(record.title()));
        icon.setStyle("-fx-font-size: 30px;");
        iconContainer.getChildren().add(icon);

        VBox contentBox = new VBox(2);
        Label title = new Label(record.title());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");

        Label taskDesc = new Label("Task: " + record.message());
        taskDesc.setStyle("-fx-text-fill: #555; -fx-font-size: 14px;");

        Label rewardLabel = new Label("Reward: " + getRewardFromRecord(record));
        rewardLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 14px;");

        contentBox.getChildren().addAll(title, taskDesc, rewardLabel);

        VBox rightBox = new VBox(5);
        rightBox.setAlignment(Pos.TOP_RIGHT);

        String time = record.scheduled_Time().format(DateTimeFormatter.ofPattern("HH:mm"));
        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnCheck = new Button("✓");
        btnCheck.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-cursor: hand;");
        btnCheck.setOnAction(e -> handleTaskAction(record, true, card));

        Button btnCross = new Button("✕");
        btnCross.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-cursor: hand;");
        btnCross.setOnAction(e -> handleTaskAction(record, false, card));

        actions.getChildren().addAll(btnCheck, btnCross);
        rightBox.getChildren().addAll(timeLabel, actions);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        mainLayout.getChildren().addAll(iconContainer, contentBox, spacer, rightBox);
        card.getChildren().add(mainLayout);

        return card;
    }

    private void handleTaskAction(NotificationRecord record, boolean completed, VBox card) {
        var repo = uk.ac.soton.comp2300.App.getInstance().getRepository();

        // Find the specific notification in the backend repository
        for (var note : repo.getAllNotifications()) {
            if (note.getId().equals(record.id())) {
                if (completed) {
                    System.out.println("Rewarding user for: " + record.title());
                    note.setStatus(uk.ac.soton.comp2300.model.Notification.Status.TASK_COMPLETED);
                    // Note: You could also call note.setCompleted(java.time.LocalDateTime.now()) here
                } else {
                    // If Mariya clicks 'X', we mark it so it doesn't show up again
                    note.setStatus(uk.ac.soton.comp2300.model.Notification.Status.TIMED_OUT);
                }
                break;
            }
        }

        // Remove the card from the current UI list
        notificationList.getChildren().remove(card);
    }

    private String getIconForDevice(String deviceName) {
        if (deviceName == null) return "📱";
        return switch (deviceName.toLowerCase()) {
            case "printer" -> "🖨️";
            case "dish washer", "dishwasher" -> "🧼";
            case "tumble dryer", "dryer" -> "🧺";
            case "washing machine" -> "🧼";
            case "hallway light", "radiator" -> "💡";
            case "television", "tv" -> "📺";
            case "fan" -> "🌀";
            default -> "📱";
        };
    }

    private String getRewardFromRecord(NotificationRecord record) {
        if (record.message().contains("Reward:")) {
            return record.message().split("Reward:")[1].trim();
        }
        return "None";
    }

    @Override
    public void initialise() {
        uk.ac.soton.comp2300.App.getInstance().getNotificationLogic().setListener(this);
    }
}