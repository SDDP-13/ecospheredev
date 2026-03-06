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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * NotificationScene: Displays active alerts for appliances and tasks.
 * Includes time-based filtering to ensure future alerts stay hidden.
 */
public class NotificationScene extends BaseScene implements NotificationListenerInterface {
    private VBox notificationList;

    public NotificationScene(MainWindow mainWindow) { super(mainWindow); }

    @Override
    public void build() {
        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        // Light lavender background matching the mockup style
        root.getStyleClass().add("root-light");

        // Header Section
        StackPane header = new StackPane();
        header.setPadding(new Insets(20, 10, 20, 10));
        header.setPrefWidth(mainWindow.getWidth());

        Button btnBack = new Button("←");
        btnBack.setStyle("-fx-background-color: transparent; -fx-font-size: 28px; -fx-cursor: hand;");
        btnBack.setOnAction(e -> mainWindow.loadScene(new MenuScene(mainWindow)));
        StackPane.setAlignment(btnBack, Pos.CENTER_LEFT);

        Label title = new Label("Notifications");
        title.getStyleClass().add("title-xlarge-dark");
        StackPane.setAlignment(title, Pos.CENTER);

        header.getChildren().addAll(title, btnBack);

        // List Container
        notificationList = new VBox(15);
        notificationList.setAlignment(Pos.TOP_CENTER);
        notificationList.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(notificationList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

        VBox mainLayout = new VBox(header, scrollPane);
        root.getChildren().add(mainLayout);

        /* Sync Logic: Only show notifications that are 'SENT'
           and whose scheduled time is now or in the past.
        */
        var repo = uk.ac.soton.comp2300.App.getInstance().getRepository();
        LocalDateTime now = LocalDateTime.now();

        for (var note : repo.getAllNotifications()) {
            boolean isTimeToShow = now.isAfter(note.getScheduled_Time()) || now.isEqual(note.getScheduled_Time());

            if (note.getStatus() == uk.ac.soton.comp2300.model.Notification.Status.SENT && isTimeToShow) {
                notificationList.getChildren().add(0, createNotificationCard(new NotificationRecord(
                        note.getId(),
                        note.getTitle(),
                        note.getMessage(),
                        note.getScheduled_Time(),
                        note.getType()
                )));
            }
        }
    }

    @Override
    public void onNotificationSent(NotificationRecord record) {
        Platform.runLater(() -> {
            LocalDateTime now = LocalDateTime.now();
            // Live update: Only pop up if it's actually time
            if (now.isAfter(record.scheduled_Time()) || now.isEqual(record.scheduled_Time())) {
                notificationList.getChildren().add(0, createNotificationCard(record));
            }
        });
    }

    /**
     * Creates a card with a bold appliance name and action buttons.
     */
    /**
     * Creates a card that focuses on the action instruction instead of rewards.
     */
    private VBox createNotificationCard(NotificationRecord record) {
        VBox card = new VBox(5);
        card.getStyleClass().add("card");
        card.setMaxWidth(400);

        HBox mainLayout = new HBox(12);
        mainLayout.setAlignment(Pos.CENTER_LEFT);

        String deviceName = record.title();
        String imageName = deviceName.replace(" ", "") + ".png";

        if (deviceName.equalsIgnoreCase("Dryer") || deviceName.equalsIgnoreCase("Tumble Dryer")) {
            imageName = "WashingMachine.png";
        }

        javafx.scene.image.ImageView iconView = new javafx.scene.image.ImageView();
        try {
            var stream = getClass().getResourceAsStream("/images/" + imageName);
            if (stream != null) {
                iconView.setImage(new javafx.scene.image.Image(stream));
                iconView.setFitWidth(35); // Scaled for the square
                iconView.setPreserveRatio(true);

                // Apply white tint for dark mode icons
                if (root.getStyleClass().contains("dark-mode")) {
                    javafx.scene.effect.ColorAdjust whiteTint = new javafx.scene.effect.ColorAdjust();
                    whiteTint.setBrightness(1.0);
                    iconView.setEffect(whiteTint);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load scene icon: " + imageName);
        }

        // --- NEW: White Square Container ---
        StackPane iconContainer = new StackPane(iconView);
        iconContainer.setPrefSize(55, 55);
        iconContainer.setMinSize(55, 55);
        // Permanent white background
        iconContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 4, 0, 0, 2);");

        VBox contentBox = new VBox(2);
        Label title = new Label(record.title());
        title.getStyleClass().add("title-large");

        String action = getActionInstruction(record.title());
        Label instructionLabel = new Label("It's time to " + action + " this appliance");
        instructionLabel.getStyleClass().add("title-medium");
        instructionLabel.setWrapText(true);
        instructionLabel.setMaxWidth(200);

        contentBox.getChildren().addAll(title, instructionLabel);

        VBox rightBox = new VBox(2);
        rightBox.setAlignment(Pos.TOP_RIGHT);

        String timeStr = record.scheduled_Time().format(DateTimeFormatter.ofPattern("HH:mm"));
        Label timeLabel = new Label(timeStr);
        timeLabel.getStyleClass().add("title-medium-small-dark");

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button btnCheck = new Button("✓");
        btnCheck.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-cursor: hand; -fx-padding: 0;");
        btnCheck.setOnAction(e -> handleTaskAction(record, true, card));

        Button btnCross = new Button("✕");
        btnCross.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-cursor: hand; -fx-padding: 0;");
        btnCross.setOnAction(e -> handleTaskAction(record, false, card));

        actions.getChildren().addAll(btnCheck, btnCross);
        rightBox.getChildren().addAll(timeLabel, actions);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Replaced direct iconView with the new iconContainer
        mainLayout.getChildren().addAll(iconContainer, contentBox, spacer, rightBox);
        card.getChildren().add(mainLayout);

        return card;
    }
    /**
     * Helper to determine if an appliance should be turned 'on' or 'off'.
     */
    private String getActionInstruction(String deviceName) {
        if (deviceName == null) return "check";
        return switch (deviceName.toLowerCase()) {
            case "hallway light", "radiator", "television", "tv", "fan" -> "turn off";
            case "printer", "dish washer", "dishwasher", "tumble dryer", "dryer", "washing machine" -> "turn on";
            default -> "check";
        };
    }

    private void handleTaskAction(NotificationRecord record, boolean completed, VBox card) {
        var app = uk.ac.soton.comp2300.App.getInstance();
        var repo = app.getRepository();

        for (var note : repo.getAllNotifications()) {
            if (note.getId().equals(record.id())) {
                if (completed) {
                    note.setStatus(uk.ac.soton.comp2300.model.Notification.Status.TASK_COMPLETED);
                    app.incrementCompletedTasks();

                    var report = app.getSavingsReportForDevice(note.getTitle());
                    app.addReportSavings(report); // This updates the Dashboard variables
                } else {
                    note.setStatus(uk.ac.soton.comp2300.model.Notification.Status.TIMED_OUT);
                }
                break;
            }
        }
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
            default -> "🛠️";
        };
    }

    private String getRewardFromRecord(NotificationRecord record) {
        if (record.message().contains("Reward:")) {
            return record.message().substring(record.message().indexOf("Reward:")).trim();
        }
        return "Reward: None";
    }

    @Override
    public void initialise() {
        uk.ac.soton.comp2300.App.getInstance().getNotificationLogic().setListener(this);
    }
}
