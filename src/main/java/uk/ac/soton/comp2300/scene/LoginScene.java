package uk.ac.soton.comp2300.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;

public class LoginScene extends BaseScene {

    public LoginScene(MainWindow mainWindow) {
        super(mainWindow);
    }

    @Override
    public void build() {
        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.setStyle("-fx-background-color: white;");

        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(40));

        Label title = new Label("Echosphere");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));

        Label userIcon = new Label("👤");
        userIcon.setStyle("-fx-font-size: 60px; -fx-text-fill: #333;");

        Label loginHeader = new Label("Login");
        loginHeader.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        VBox userBox = createInputField("Player1", "Enter your username");
        VBox passBox = createInputField("Pass1", "Enter your password");

        Hyperlink forgotPass = new Hyperlink("Forgot your password?");
        forgotPass.setStyle("-fx-text-fill: blue; -fx-underline: true;");

        Button loginBtn = new Button("✓  Login");
        loginBtn.setPrefWidth(180);
        loginBtn.setStyle("-fx-background-color: #E6E0F8; -fx-border-color: #CCC; -fx-text-fill: black;");

        // This takes you to the planet view (MenuScene)
        loginBtn.setOnAction(e -> mainWindow.loadScene(new MenuScene(mainWindow)));

        Button createAccBtn = new Button("✪  Create Account");
        createAccBtn.setPrefWidth(220);
        createAccBtn.setStyle("-fx-background-color: #F0EAFB; -fx-background-radius: 15; -fx-text-fill: #333;");

        container.getChildren().addAll(title, userIcon, loginHeader, userBox, passBox, forgotPass, loginBtn, createAccBtn);
        root.getChildren().add(container);
    }

    private VBox createInputField(String text, String prompt) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);

        TextField field = new TextField(text);
        field.setMaxWidth(250);
        field.setPrefHeight(45);
        field.setStyle("-fx-background-color: #F0EAFB; -fx-border-color: transparent transparent grey transparent;");

        Label subText = new Label(prompt);
        subText.setStyle("-fx-font-size: 10px; -fx-text-fill: grey;");

        box.getChildren().addAll(field, subText);
        return box;
    }

    @Override
    public void initialise() {}
}