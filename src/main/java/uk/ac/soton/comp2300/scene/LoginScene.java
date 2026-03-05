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
        root.getStyleClass().add("root-dark");

        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(40));

        Label title = new Label("Echosphere");
        title.getStyleClass().add("title-xlarge-font");

        Label userIcon = new Label("👤");
        userIcon.getStyleClass().add("user-icon");

        Label loginHeader = new Label("Login");
        loginHeader.getStyleClass().add("title-large-font");

        VBox userBox = createInputField("Player1", "Enter your username");
        //userBox.getStyleClass().add();
        VBox passBox = createInputField("Pass1", "Enter your password");

        Hyperlink forgotPass = new Hyperlink("Forgot your password?");
        forgotPass.getStyleClass().add("forgot-pass");

        Button loginBtn = new Button("✓  Login");
        loginBtn.setPrefWidth(180);
        loginBtn.getStyleClass().add("login-button");

        // This takes you to the planet view (MenuScene)
        loginBtn.setOnAction(e -> mainWindow.loadScene(new MenuScene(mainWindow)));

        Button createAccBtn = new Button("✪  Create Account");
        createAccBtn.setPrefWidth(220);
        createAccBtn.getStyleClass().add("create-account-button");

        container.getChildren().addAll(title, userIcon, loginHeader, userBox, passBox, forgotPass, loginBtn, createAccBtn);
        root.getChildren().add(container);
    }

    private VBox createInputField(String text, String prompt) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);

        TextField field = new TextField(text);
        field.setMaxWidth(250);
        field.setPrefHeight(45);
        field.getStyleClass().add("field");

        Label subText = new Label(prompt);
        subText.getStyleClass().add("sub-text");

        box.getChildren().addAll(field, subText);
        return box;
    }

    @Override
    public void initialise() {}
}