package uk.ac.soton.comp2300.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;

public class SettingsScene extends BaseScene {
    public SettingsScene(MainWindow mainWindow) {
        super(mainWindow);
    }

    @Override
    public void build() {
        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.setStyle("-fx-background-color: black;");

        // Back button to return to the menu
        Button btnBack = new Button("⬅");
        btnBack.setPrefSize(50, 50);
        btnBack.getStyleClass().add("menu-icon-button");
        btnBack.setOnAction(e -> mainWindow.loadScene(new MenuScene(mainWindow)));

        StackPane.setAlignment(btnBack, Pos.TOP_LEFT);
        StackPane.setMargin(btnBack, new Insets(20));

        VBox container = new VBox(30);
        container.setAlignment(Pos.CENTER);

        Label title = new Label("Settings");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold;");

        // Logout Button
        Button btnLogout = new Button("Log Out");
        btnLogout.setPrefSize(200, 50);
        // Using a reddish style to indicate an exit action
        btnLogout.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");

        // Navigation logic to return to Login
        btnLogout.setOnAction(e -> mainWindow.loadScene(new LoginScene(mainWindow)));

        container.getChildren().addAll(title, btnLogout);
        root.getChildren().addAll(container, btnBack);
    }

    @Override public void initialise() {}
}