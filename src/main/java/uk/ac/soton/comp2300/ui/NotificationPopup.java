package uk.ac.soton.comp2300.ui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.util.Duration;
import uk.ac.soton.comp2300.event.NotificationRecord;
import uk.ac.soton.comp2300.scene.NotificationScene;

public class NotificationPopup {

    /**
     * @param mainWindow The main window instance needed for scene switching.
     * @param record The notification data.
     */
    public static void show(MainWindow mainWindow, NotificationRecord record) {
        Popup popup = new Popup();
        var stage = mainWindow.getStage();

        VBox container = new VBox(8);
        container.setStyle("-fx-background-color: white; -fx-border-color: #D1C4E9; " +
                "-fx-border-radius: 12; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);");
        container.setPadding(new Insets(15));
        container.setPrefWidth(320);

        Label title = new Label(record.title());
        title.setStyle("-fx-font-weight: 800; -fx-font-size: 16px;");

        Label msg = new Label("It's time to check this appliance");
        msg.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        // CHECK BUTTON: Increments the task counter
        Button btnCheck = new Button("✓");
        btnCheck.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-cursor: hand; -fx-padding: 0 5 0 0;");
        btnCheck.setOnAction(e -> {
            var repo = uk.ac.soton.comp2300.App.getInstance().getRepository();
            for (var note : repo.getAllNotifications()) {
                if (note.getId().equals(record.id())) {
                    note.setStatus(uk.ac.soton.comp2300.model.Notification.Status.TASK_COMPLETED);
                    break;
                }
            }
            uk.ac.soton.comp2300.App.getInstance().incrementCompletedTasks();
            popup.hide();
        });

        Button btnViewAll = new Button("View All");
        btnViewAll.setStyle("-fx-background-color: #DCD0FF; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 11px;");
        btnViewAll.setOnAction(e -> {
            popup.hide();
            mainWindow.loadScene(new NotificationScene(mainWindow));
        });

        Button btnClose = new Button("✕");
        btnClose.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; -fx-cursor: hand;");
        btnClose.setOnAction(e -> popup.hide());

        // Added btnCheck to the action row
        actions.getChildren().addAll(btnCheck, btnViewAll, btnClose);
        container.getChildren().addAll(title, msg, actions);

        popup.getContent().add(container);

        popup.setX(stage.getX() + 20);
        popup.setY(stage.getY() + 50);

        PauseTransition stayVisible = new PauseTransition(Duration.seconds(7));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), container);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        stayVisible.setOnFinished(e -> fadeOut.play());
        fadeOut.setOnFinished(e -> popup.hide());

        popup.show(stage);
        stayVisible.play();
    }}