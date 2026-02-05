package uk.ac.soton.comp2300.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;

public class NotificationScene extends BaseScene {
    public NotificationScene(MainWindow mainWindow) { super(mainWindow); }

    @Override
    public void build() {
        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.setStyle("-fx-background-color: black;");

        Button btnBack = new Button("⬅");
        btnBack.setPrefSize(50, 50);
        btnBack.getStyleClass().add("menu-icon-button");
        btnBack.setOnAction(e -> mainWindow.loadScene(new MenuScene(mainWindow)));
        StackPane.setAlignment(btnBack, Pos.TOP_LEFT);
        StackPane.setMargin(btnBack, new Insets(20));

        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        Label title = new Label("Notifications");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold;");

        container.getChildren().add(title);
        root.getChildren().addAll(container, btnBack);

        // Inside your NotificationScene build() method
        VBox settingsList = new VBox(20);
        settingsList.setAlignment(Pos.CENTER);

        // Creating a toggle for Energy Alerts
        HBox energyAlertRow = new HBox(50);
        energyAlertRow.setAlignment(Pos.CENTER);
        Label energyLabel = new Label("Energy Usage Alerts");
        energyLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        CheckBox energyToggle = new CheckBox(); // This acts as our toggle switch
        energyToggle.setSelected(true);

        energyAlertRow.getChildren().addAll(energyLabel, energyToggle);
        settingsList.getChildren().add(energyAlertRow);

        // Add to your main container
        container.getChildren().add(settingsList);
    }

    @Override public void initialise() {}
}