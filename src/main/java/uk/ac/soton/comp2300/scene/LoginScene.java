package uk.ac.soton.comp2300.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uk.ac.soton.comp2300.App;
import uk.ac.soton.comp2300.model.Setting;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;

public class LoginScene extends BaseScene {

    private StackPane overlayLayer;

    public LoginScene(MainWindow mainWindow) {
        super(mainWindow);
    }

    @Override
    public void build() {
        Setting.init();

        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.getStyleClass().add("root-dark");

        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(40));

        Label title = new Label("Echosphere");
        title.getStyleClass().add("title-xlarge-font");

        Label userIcon = new Label("\uD83D\uDC64");
        userIcon.getStyleClass().add("user-icon");

        Label loginHeader = new Label("Login");
        loginHeader.getStyleClass().add("title-large-font");

        TextField usernameField = new TextField(Setting.getUsername());
        usernameField.setPromptText("Enter your username");
        usernameField.setMaxWidth(250);
        usernameField.setPrefHeight(45);
        usernameField.getStyleClass().add("field");

        Label usernameHint = new Label("Enter your username");
        usernameHint.getStyleClass().add("sub-text");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setMaxWidth(250);
        passwordField.setPrefHeight(45);
        passwordField.getStyleClass().add("field");

        Label passwordHint = new Label("Enter your password");
        passwordHint.getStyleClass().add("sub-text");

        VBox userBox = new VBox(5, usernameField, usernameHint);
        userBox.setAlignment(Pos.CENTER);

        VBox passBox = new VBox(5, passwordField, passwordHint);
        passBox.setAlignment(Pos.CENTER);

        Label statusLabel = new Label("Your account details are stored locally on this device.");
        statusLabel.getStyleClass().add("sub-text");

        Button loginBtn = new Button("\u2713  Login");
        loginBtn.setPrefWidth(180);
        loginBtn.getStyleClass().add("login-button");
        loginBtn.setOnAction(e -> {
            Setting.LoginResult result = Setting.login(usernameField.getText(), passwordField.getText());
            statusLabel.setText(result.msg);
            if (result.ok) {
                App.getInstance().getCookieStorageService().login(result.username);
                mainWindow.loadScene(new MenuScene(mainWindow));
            }
        });

        Button createAccountBtn = new Button("+  Create Account");
        createAccountBtn.setPrefWidth(180);
        createAccountBtn.getStyleClass().add("create-account-button");
        createAccountBtn.setOnAction(e -> showOverlay(new CreateAccountDialog()));

        usernameField.setOnAction(e -> loginBtn.fire());
        passwordField.setOnAction(e -> loginBtn.fire());

        overlayLayer = new StackPane();
        overlayLayer.setPickOnBounds(false);

        container.getChildren().addAll(
                title,
                userIcon,
                loginHeader,
                userBox,
                passBox,
                statusLabel,
                loginBtn,
                createAccountBtn
        );

        StackPane content = new StackPane(container, overlayLayer);
        root.getChildren().add(content);
    }

    private void showOverlay(Node content) {
        StackPane dim = new StackPane();
        dim.getStyleClass().add("overlay-dim");
        dim.setPickOnBounds(true);
        dim.addEventFilter(MouseEvent.MOUSE_CLICKED, MouseEvent::consume);

        StackPane wrapper = new StackPane(content);
        wrapper.setPadding(new Insets(60, 18, 60, 18));
        StackPane.setAlignment(content, Pos.CENTER);

        overlayLayer.getChildren().setAll(dim, wrapper);
    }

    private void clearOverlay() {
        overlayLayer.getChildren().clear();
    }

    private class CreateAccountDialog extends VBox {

        private CreateAccountDialog() {
            super(12);

            setPadding(new Insets(18));
            setMaxWidth(420);
            setAlignment(Pos.CENTER_LEFT);
            getStyleClass().addAll("button-shape-rounded-large", "effect-1", "root-light");

            Label title = new Label("Create New Account");
            title.getStyleClass().add("title-large");

            TextField usernameField = new TextField();
            usernameField.setPromptText("New username");
            usernameField.setPrefHeight(40);

            PasswordField passwordField = new PasswordField();
            passwordField.setPromptText("New password");
            passwordField.setPrefHeight(40);

            PasswordField confirmPasswordField = new PasswordField();
            confirmPasswordField.setPromptText("Confirm password");
            confirmPasswordField.setPrefHeight(40);

            Label status = new Label("Create a local account to use on this device.");
            status.getStyleClass().add("label-small");

            Button cancel = new Button("Cancel");
            cancel.setPrefWidth(140);
            cancel.setPrefHeight(40);
            cancel.getStyleClass().addAll("button-tertiary", "button-shape-rounded");
            cancel.setOnAction(e -> clearOverlay());

            Button create = new Button("Create");
            create.setPrefWidth(140);
            create.setPrefHeight(40);
            create.getStyleClass().addAll("button-primary", "button-shape-rounded");
            create.setOnAction(e -> {
                Setting.RegisterResult result = Setting.createAccount(
                        usernameField.getText(),
                        passwordField.getText(),
                        confirmPasswordField.getText()
                );
                status.setText(result.msg);
                if (result.ok) {
                    App.getInstance().getCookieStorageService().login(result.username);
                    clearOverlay();
                    mainWindow.loadScene(new MenuScene(mainWindow));
                }
            });

            confirmPasswordField.setOnAction(e -> create.fire());

            HBox actions = new HBox(10, cancel, create);
            actions.setAlignment(Pos.CENTER_RIGHT);

            getChildren().addAll(title, usernameField, passwordField, confirmPasswordField, status, actions);
        }
    }

    @Override
    public void initialise() {}
}
