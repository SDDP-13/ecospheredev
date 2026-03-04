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
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #311B92;");

        // --- UPDATED MESSAGE LOGIC ---
        Label message = new Label();
        if (record.type() == uk.ac.soton.comp2300.model.Notification.Type.GAME_EVENT) {
            // Shows "Your eco-influence is growing. ⭐"
            message.setText(record.message());
        } else {
            // Reverts back to original appliance reminder
            message.setText("It's time to check this appliance");
        }

        message.setWrapText(true);
        message.setStyle("-fx-text-fill: #7986CB;");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button btnViewAll = new Button("View All");
        btnViewAll.setStyle("-fx-background-color: #DCD0FF; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 11px;");
        btnViewAll.setOnAction(e -> {
            popup.hide();
            mainWindow.loadScene(new NotificationScene(mainWindow));
        });

        Button btnClose = new Button("✕");
        btnClose.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; -fx-cursor: hand;");
        btnClose.setOnAction(e -> popup.hide());

        // Logic branching for Level Up vs Appliances
        if (record.type() == uk.ac.soton.comp2300.model.Notification.Type.GAME_EVENT) {
            actions.getChildren().addAll(btnViewAll, btnClose);
        } else {
            Button btnCheck = new Button("✓");
            btnCheck.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-cursor: hand; -fx-padding: 0 5 0 0;");

            btnCheck.setOnAction(e -> {
                var app = uk.ac.soton.comp2300.App.getInstance();
                var repo = app.getRepository();
                var controller = app.getGameController();
                String deviceName = record.title();

                for (var note : repo.getAllNotifications()) {
                    if (note.getId().equals(record.id())) {
                        note.setStatus(uk.ac.soton.comp2300.model.Notification.Status.TASK_COMPLETED);
                        break;
                    }
                }

                app.addReportSavings(new uk.ac.soton.comp2300.model.EcoSavingsReport(
                        app.getEnergySavedForDevice(deviceName) * 0.15,
                        app.getEnergySavedForDevice(deviceName) * 0.2));

                app.incrementCompletedTasks();
                app.addXp(20);

                controller.addResource(uk.ac.soton.comp2300.model.Resource.MONEY, 100);
                controller.addResource(uk.ac.soton.comp2300.model.Resource.WOOD, 50);
                controller.addResource(uk.ac.soton.comp2300.model.Resource.METAL, 20);
                controller.addResource(uk.ac.soton.comp2300.model.Resource.STONE, 10);

                popup.hide();
            });

            actions.getChildren().addAll(btnCheck, btnViewAll, btnClose);
        }

        container.getChildren().addAll(title, message, actions);
        popup.getContent().add(container);

        // (Positioning and Transitions remain as before...)
        popup.setX(stage.getX() + 20);
        popup.setY(stage.getY() + 50);
        popup.show(stage);
    }
}