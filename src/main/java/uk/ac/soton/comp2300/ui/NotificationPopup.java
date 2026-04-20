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

        String deviceName = record.title();
        String imageName = deviceName.replace(" ", "") + ".png";

        if (deviceName.equalsIgnoreCase("Dryer")) {
            imageName = "WashingMachine.png";
        }

        //Check for records of type Game_event
        if (record.type() == uk.ac.soton.comp2300.model.Notification.Type.GAME_EVENT) {

            /**Check for records referring to leveling up and then select the correct .png**/
            if (deviceName.equals("Level Up!")) {
                String lvl = record.id();
                int level = Integer.parseInt(lvl.replace("LVL_UP_", ""));
                imageName = "Lvl_" + level + ".png";
            }

        }

        javafx.scene.image.ImageView iconView = new javafx.scene.image.ImageView();
        try {
            var stream = NotificationPopup.class.getResourceAsStream("/images/" + imageName);
            if (stream != null) {
                iconView.setImage(new javafx.scene.image.Image(stream));
                iconView.setFitWidth(45);
                iconView.setPreserveRatio(true);
            }
        } catch (Exception e) {
            System.err.println("Could not load notification icon: " + imageName);
        }

        Label title = new Label(record.title());
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #311B92;");

        Label message = new Label();
        if (record.type() == uk.ac.soton.comp2300.model.Notification.Type.GAME_EVENT) {
            message.setText(record.message());
        } else {
            message.setText("It's time to check this appliance");
        }

        message.setWrapText(true);
        message.setStyle("-fx-text-fill: #7986CB;");

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button btnClose = new Button("✕");
        btnClose.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; -fx-cursor: hand;");
        btnClose.setOnAction(e -> popup.hide());

        if (record.type() == uk.ac.soton.comp2300.model.Notification.Type.GAME_EVENT) {
            Button btnDashboard = new Button("Dashboard");
            btnDashboard.setStyle("-fx-background-color: #DCD0FF; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 11px;");
            btnDashboard.setOnAction(e -> {
                popup.hide();
                mainWindow.loadScene(new uk.ac.soton.comp2300.scene.DashboardScene(mainWindow));
            });
            actions.getChildren().addAll(btnDashboard, btnClose);
        } else {
            Button btnViewAll = new Button("View All");
            btnViewAll.setStyle("-fx-background-color: #DCD0FF; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 11px;");
            btnViewAll.setOnAction(e -> {
                popup.hide();
                mainWindow.loadScene(new NotificationScene(mainWindow));
            });

            Button btnCheck = new Button("✓");
            btnCheck.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-cursor: hand; -fx-padding: 0 5 0 0;");
            btnCheck.setOnAction(e -> {
                var app = uk.ac.soton.comp2300.App.getInstance();
                var repo = app.getRepository();
                var controller = app.getGameController();
                String name = record.title();

                for (var note : repo.getAllNotifications()) {
                    if (note.getId().equals(record.id())) {
                        note.setStatus(uk.ac.soton.comp2300.model.Notification.Status.TASK_COMPLETED);
                        break;
                    }
                }
                app.addReportSavings(app.getSavingsReportForDevice(name));
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

        container.getChildren().addAll(iconView, title, message, actions);
        popup.getContent().add(container);

        popup.setX(stage.getX() + 20);
        popup.setY(stage.getY() + 50);
        popup.show(stage);
    }
}
