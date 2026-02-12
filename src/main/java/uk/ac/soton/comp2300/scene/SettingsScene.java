package uk.ac.soton.comp2300.scene;

import java.util.function.Function;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp2300.component.ToggleSwitch;
import uk.ac.soton.comp2300.model.Setting;
import uk.ac.soton.comp2300.model.Setting.SettingOption;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;

import java.util.function.Consumer;

public class SettingsScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(SettingsScene.class);

    private static final double BASE = 44.0;

    // Hard coded
    private static final String USERNAME = "Player1";
    private static final String PASSWORD = "Pass1";

    private StackPane overlayLayer;

    public SettingsScene(MainWindow mainWindow) {
        super(mainWindow);
    }

    @Override
    public void build() {
        Setting.init();

        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.setStyle("-fx-background-color: #F6F3FB;");

        Button btnBack = new Button("←");
        btnBack.setPrefSize(BASE, BASE);
        btnBack.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-font-size: 18px;" +
                        "-fx-text-fill: #1F1F1F;"
        );
        btnBack.getStyleClass().add("menu-icon-button");
        btnBack.setOnAction(e -> mainWindow.loadScene(new MenuScene(mainWindow)));

        StackPane.setAlignment(btnBack, Pos.TOP_LEFT);
        StackPane.setMargin(btnBack, new Insets(20));

        Label title = new Label("Settings");
        title.setStyle(
                "-fx-text-fill: #1F1F1F;" +
                        "-fx-font-size: " + (BASE * 0.6) + "px;" +
                        "-fx-font-weight: 800;"
        );

        Button blbGear = new Button("\u2699");
        blbGear.setPrefSize(BASE, BASE);
        blbGear.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-font-size: 18px;" +
                        "-fx-text-fill: #1F1F1F;"
        );

        Region topSpacerL = new Region();
        Region topSpacerR = new Region();
        HBox.setHgrow(topSpacerL, Priority.ALWAYS);
        HBox.setHgrow(topSpacerR, Priority.ALWAYS);

        HBox topBar = new HBox(10, btnBack, topSpacerL, title, topSpacerR, blbGear);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(18, 18, 10, 18));

        VBox settingsBox = settingSelectionBox(1f);
        VBox.setMargin(settingsBox, new Insets(10, 20, 10, 20));
        settingsBox.setMaxWidth(BASE * 8.2);

        Button btnAccountDetails = makeActionButton("Account Details", "#E8DDFB", "#2B2B2B");
        Button btnLogout = makeActionButton("Logout", "#E8DDFB", "#2B2B2B");
        Button btnDelete = makeActionButton("Delete Account", "#F4B7C0", "#2B2B2B");

        btnLogout.setOnAction(e -> mainWindow.loadScene(new LoginScene(mainWindow)));

        btnAccountDetails.setOnAction(e -> showAccountDetailsOverlay());

        btnDelete.setOnAction(e -> showDeleteAccountFlow());

        VBox container = new VBox(BASE * 0.35, settingsBox, btnAccountDetails, btnLogout, btnDelete);
        container.setAlignment(Pos.TOP_CENTER);
        container.setPadding(new Insets(0, BASE * 0.45, BASE * 0.45, BASE * 0.45));
        container.setMaxWidth(420);

        VBox screen = new VBox(0, topBar, container);
        screen.setAlignment(Pos.TOP_CENTER);

        overlayLayer = new StackPane();
        overlayLayer.setPickOnBounds(false);

        StackPane rootStack = new StackPane(screen, overlayLayer);
        root.getChildren().add(rootStack);
    }

    @Override
    public void initialise() {
        // Nothing.
    }


    private void clearOverlay() {
        overlayLayer.getChildren().clear();
    }

    private void showOverlay(Node content) {
        StackPane dim = new StackPane();
        dim.setStyle("-fx-background-color: rgba(0,0,0,0.25);");
        dim.setPickOnBounds(true);
        dim.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> e.consume());

        StackPane wrapper = new StackPane(content);
        wrapper.setPadding(new Insets(100, 18, 500, 18));
        StackPane.setAlignment(content, Pos.CENTER);

        overlayLayer.getChildren().setAll(dim, wrapper);
    }

    private void showAccountDetailsOverlay() {
        VBox box = new VBox(14);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(18));
        box.setMaxWidth(440);
        box.setStyle(
                "-fx-background-color: #EDE7F7;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-radius: 18;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 18, 0.25, 0, 6);"
        );

        Label title = new Label("Account Details");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #1F1F1F;");

        Label hint = new Label("Hard coded, password is `Pass1`");
        hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #5A5A5A;");

        VBox rows = new VBox(10);
        rows.getChildren().addAll(
                new EditableRow(
                        "Username",
                        USERNAME,
                        this::showChangeUsernameOverlay,
                        this::showOverlay,
                        this::clearOverlay,
                        PASSWORD
                ),
                new EditableRow(
                        "Password",
                        "\u25CF".repeat(Math.max(4, PASSWORD.length())),
                        this::showChangePasswordOverlay,
                        this::showOverlay,
                        this::clearOverlay,
                        PASSWORD
                )
        );


        Button close = new Button("Close");
        styleSmallButton(close, "#E8DDFB", "#2B2B2B");
        close.setOnAction(e -> clearOverlay());

        HBox actions = new HBox(10, close);
        actions.setAlignment(Pos.CENTER_RIGHT);

        box.getChildren().addAll(title, hint, rows, actions);

        showOverlay(box);
    }

    private static class EditableRow extends HBox {

        public EditableRow(String label,
                        String value,
                        Runnable onEdit,
                        Consumer<Node> showOverlay,
                        Runnable clearOverlay,
                        String correctPassword) {

            super(12);
            setAlignment(Pos.CENTER_LEFT);
            setPadding(new Insets(10, 12, 10, 12));
            setStyle(
                    "-fx-background-color: rgba(255,255,255,0.55);" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-radius: 12;"
            );

            VBox text = new VBox(3);

            Label l = new Label(label);
            l.setStyle("-fx-font-size: 13px; -fx-font-weight: 800; -fx-text-fill: #1F1F1F;");

            Label v = new Label(value);
            v.setStyle("-fx-font-size: 12px; -fx-text-fill: #4A4A4A;");

            text.getChildren().addAll(l, v);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button edit = new Button("Edit");
            edit.setStyle(
                    "-fx-background-color: #E8DDFB;" +
                            "-fx-text-fill: #2B2B2B;" +
                            "-fx-font-size: 13px;" +
                            "-fx-font-weight: 800;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-radius: 10;"
            );
            edit.setPrefHeight(34);

            edit.setOnAction(e -> {

                PermissionDialog dialog = new PermissionDialog(
                        "Permission Required",
                        "Enter password to edit " + label,
                        entered -> Setting.checkPassword(entered),
                        res -> {
                            clearOverlay.run();
                            if (res.ok) onEdit.run();
                        }
                );

                showOverlay.accept(dialog);
            });

            getChildren().addAll(text, spacer, edit);
        }
    }


    private void showChangeUsernameOverlay() {
        ChangeUsernameDialog dialog = new ChangeUsernameDialog(
                USERNAME,
                newUsername -> {
                    // Hard coded - do nothing for now
                    logger.info("Requested username change to: {}", newUsername);
                    clearOverlay();
                },
                () -> {
                    clearOverlay();
                }
        );

        showOverlay(dialog);
    }

    private void showChangePasswordOverlay() {
        ChangePasswordDialog dialog = new ChangePasswordDialog(
                newPassword -> {
                    // Hard coded - do nothing for now
                    logger.info("Requested password change (length={}): (not applied)", newPassword.length());
                    clearOverlay();
                },
                () -> {
                    clearOverlay();
                }
        );

        showOverlay(dialog);
    }

    private class ChangeUsernameDialog extends VBox {

        public ChangeUsernameDialog(String currentUsername,
                                    Consumer<String> onConfirm,
                                    Runnable onCancel) {
            super(12);

            setPadding(new Insets(18));
            setMaxWidth(460);
            setAlignment(Pos.CENTER_LEFT);
            setStyle(
                    "-fx-background-color: #EDE7F7;" +
                            "-fx-background-radius: 18;" +
                            "-fx-border-radius: 18;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 18, 0.25, 0, 6);"
            );

            Label title = new Label("Create New Username");
            title.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #1F1F1F;");

            Label current = new Label("Current username: " + currentUsername);
            current.setStyle("-fx-font-size: 12px; -fx-text-fill: #5A5A5A;");

            TextField tf = new TextField();
            tf.setPromptText("Enter new username");
            tf.setPrefHeight(40);

            Label status = new Label("Note: This is hard coded (does nothing yet).");
            status.setStyle("-fx-font-size: 12px; -fx-text-fill: #5A5A5A;");

            Button cancel = new Button("Cancel");
            styleSmallButton(cancel, "#E8DDFB", "#2B2B2B");
            cancel.setOnAction(e -> onCancel.run());

            Button confirm = new Button("Confirm");
            styleSmallButton(confirm, "#F4B7C0", "#2B2B2B");

            confirm.setOnAction(e -> {
                Setting.UsernameResult res = Setting.validateUsername(tf.getText(), currentUsername);
                status.setText(res.msg);
                if (res.ok) {
                    onConfirm.accept(res.username);
                }
            });

            tf.setOnAction(e -> confirm.fire());

            HBox actions = new HBox(10, cancel, confirm);
            actions.setAlignment(Pos.CENTER_RIGHT);

            getChildren().addAll(title, current, tf, status, actions);
        }
    }

    private class ChangePasswordDialog extends VBox {

        public ChangePasswordDialog(Consumer<String> onConfirm,
                                    Runnable onCancel) {
            super(12);

            setPadding(new Insets(18));
            setMaxWidth(460);
            setAlignment(Pos.CENTER_LEFT);
            setStyle(
                    "-fx-background-color: #EDE7F7;" +
                            "-fx-background-radius: 18;" +
                            "-fx-border-radius: 18;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 18, 0.25, 0, 6);"
            );

            Label title = new Label("Create New Password");
            title.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #1F1F1F;");

            Label msg = new Label("Enter a new password and retype it.");
            msg.setStyle("-fx-font-size: 12px; -fx-text-fill: #5A5A5A;");

            PasswordField pf1 = new PasswordField();
            pf1.setPromptText("New password");
            pf1.setPrefHeight(40);

            PasswordField pf2 = new PasswordField();
            pf2.setPromptText("Retype new password");
            pf2.setPrefHeight(40);

            Label status = new Label("Note: This is hard coded (does nothing yet).");
            status.setStyle("-fx-font-size: 12px; -fx-text-fill: #5A5A5A;");

            Button cancel = new Button("Cancel");
            styleSmallButton(cancel, "#E8DDFB", "#2B2B2B");
            cancel.setOnAction(e -> onCancel.run());

            Button confirm = new Button("Confirm");
            styleSmallButton(confirm, "#F4B7C0", "#2B2B2B");

            confirm.setOnAction(e -> {
                Setting.PasswordChangeResult res =
                        Setting.validateNewPassword(pf1.getText(), pf2.getText());

                status.setText(res.msg);

                if (res.ok) {
                    onConfirm.accept(res.newPassword);
                }
            });

            pf2.setOnAction(e -> confirm.fire());

            HBox actions = new HBox(10, cancel, confirm);
            actions.setAlignment(Pos.CENTER_RIGHT);

            getChildren().addAll(title, msg, pf1, pf2, status, actions);
        }
    }


    private void showDeleteAccountFlow() {
        PermissionDialog dialog = new PermissionDialog(
                "Permission Required",
                "Enter your password to continue.",
                entered -> Setting.checkPassword(entered),
                res -> {
                    if (!res.ok) {
                        clearOverlay();
                        return;
                    }
                    showDeleteConfirmOverlay();
                }
        );

        showOverlay(dialog);
    }

    private void showDeleteConfirmOverlay() {
        VBox box = new VBox(14);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(18));
        box.setMaxWidth(460);
        box.setStyle(
                "-fx-background-color: #EDE7F7;" +
                        "-fx-background-radius: 18;" +
                        "-fx-border-radius: 18;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 18, 0.25, 0, 6);"
        );

        Label title = new Label("Confirm Deletion");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #1F1F1F;");

        Label warning = new Label("This will permanently delete your account. (does nothing)");
        warning.setStyle("-fx-font-size: 13px; -fx-font-weight: 800; -fx-text-fill: #2B2B2B;");

        CheckBox understand = new CheckBox("I understand");
        understand.setStyle("-fx-font-size: 13px; -fx-text-fill: #1F1F1F; -fx-font-weight: 700;");

        Label status = new Label("Confirm will unlock in 5 seconds…");
        status.setStyle("-fx-font-size: 12px; -fx-text-fill: #5A5A5A;");

        Button cancel = new Button("Cancel");
        styleSmallButton(cancel, "#E8DDFB", "#2B2B2B");
        cancel.setOnAction(e -> clearOverlay());

        Button confirm = new Button("Confirm (5)");
        styleSmallButton(confirm, "#F4B7C0", "#2B2B2B");
        confirm.setDisable(true);

        final BooleanProperty checkboxSelected = understand.selectedProperty();
        final boolean[] timeUnlocked = {false};
        final int[] seconds = {5};

        Timeline tl = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            seconds[0]--;
            if (seconds[0] <= 0) {
                timeUnlocked[0] = true;
                confirm.setText("Confirm");
                status.setText("Ready. Tick “I understand” and confirm.");
            } else {
                confirm.setText("Confirm (" + seconds[0] + ")");
                status.setText("Confirm will unlock in " + seconds[0] + " seconds…");
            }

            confirm.setDisable(!(timeUnlocked[0] && checkboxSelected.get()));
        }));
        tl.setCycleCount(5);
        tl.playFromStart();

        checkboxSelected.addListener((obs, oldV, newV) -> {
            confirm.setDisable(!(timeUnlocked[0] && newV));
        });

        confirm.setOnAction(e -> {
            logger.warn("Account deleted. Returning to Login.");
            tl.stop();
            clearOverlay();
            mainWindow.loadScene(new LoginScene(mainWindow));
        });

        HBox actions = new HBox(10, cancel, confirm);
        actions.setAlignment(Pos.CENTER_RIGHT);

        box.getChildren().addAll(title, warning, understand, status, actions);

        showOverlay(box);
    }


    private static class PermissionDialog extends VBox {

        private final Function<String, Setting.PermissionResult> verifier;
        private final Consumer<Setting.PermissionResult> onResult;

        public PermissionDialog(String titleText,
                                String messageText,
                                Function<String, Setting.PermissionResult> verifier,
                                Consumer<Setting.PermissionResult> onResult) {
            super(12);
            this.verifier = verifier;
            this.onResult = onResult;

            setPadding(new Insets(18));
            setMaxWidth(460);
            setAlignment(Pos.CENTER_LEFT);
            setStyle(
                    "-fx-background-color: #EDE7F7;" +
                            "-fx-background-radius: 18;" +
                            "-fx-border-radius: 18;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 18, 0.25, 0, 6);"
            );

            Label title = new Label(titleText);
            title.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #1F1F1F;");

            Label msg = new Label(messageText);
            msg.setStyle("-fx-font-size: 12px; -fx-text-fill: #5A5A5A;");

            PasswordField pf = new PasswordField();
            pf.setPromptText("Enter password");
            pf.setPrefHeight(40);

            Label status = new Label("");
            status.setStyle("-fx-font-size: 12px; -fx-text-fill: #5A5A5A;");

            Button cancel = new Button("Cancel");
            styleSmallButtonStatic(cancel, "#E8DDFB", "#2B2B2B");
            cancel.setOnAction(e -> onResult.accept(new Setting.PermissionResult(false, "Cancelled.")));

            Button confirm = new Button("Confirm");
            styleSmallButtonStatic(confirm, "#F4B7C0", "#2B2B2B");

            confirm.setOnAction(e -> {
                Setting.PermissionResult res = verifier.apply(pf.getText());
                status.setText(res.msg);
                if (res.ok) {onResult.accept(res);}
            });

            pf.setOnAction(e -> confirm.fire());

            HBox actions = new HBox(10, cancel, confirm);
            actions.setAlignment(Pos.CENTER_RIGHT);

            getChildren().addAll(title, msg, pf, status, actions);
        }

        private static void styleSmallButtonStatic(Button b, String bg, String fg) {
            b.setPrefWidth(140);
            b.setPrefHeight(40);
            b.setStyle(
                    "-fx-background-color: " + bg + ";" +
                            "-fx-text-fill: " + fg + ";" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-radius: 10;"
            );
        }
    }

    private Button makeActionButton(String text, String bg, String fg) {
        Button b = new Button(text);
        b.setPrefWidth(BASE * 5);
        b.setPrefHeight(BASE * 1.35);

        b.setStyle(
                "-fx-background-color: " + bg + ";" +
                        "-fx-text-fill: " + fg + ";" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;"
        );

        return b;
    }

    private void styleSmallButton(Button b, String bg, String fg) {
        b.setPrefWidth(140);
        b.setPrefHeight(40);
        b.setStyle(
                "-fx-background-color: " + bg + ";" +
                        "-fx-text-fill: " + fg + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;"
        );
    }

    private static class LabeledSwitch extends HBox {
        private final ToggleSwitch toggle;

        public LabeledSwitch(SettingOption setting, float scale) {
            super(12 * scale);

            Label titleLabel = new Label(setting.getTitle());
            titleLabel.setStyle(
                    "-fx-font-size: " + (16 * scale) + "px;" +
                            "-fx-font-weight: 800;" +
                            "-fx-text-fill: #1F1F1F;"
            );

            Label descLabel = new Label(setting.getDescription());
            descLabel.setWrapText(true);
            descLabel.setStyle(
                    "-fx-font-size: " + (12 * scale) + "px;" +
                            "-fx-text-fill: #5A5A5A;"
            );

            VBox textBox = new VBox(4 * scale, titleLabel, descLabel);
            textBox.setAlignment(Pos.CENTER_LEFT);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            this.toggle = new ToggleSwitch(scale * 1.5f);
            this.toggle.selectedProperty().bindBidirectional(setting.enabledProperty());

            setAlignment(Pos.CENTER_LEFT);
            setPadding(new Insets(8 * scale, 12 * scale, 8 * scale, 12 * scale));

            getChildren().addAll(textBox, spacer, toggle);
        }
    }

    private VBox settingSelectionBox(float scale) {
        VBox vbox = new VBox(8 * scale);
        vbox.setPadding(new Insets(10 * scale));
        vbox.setMaxWidth(420);

        for (SettingOption setting : Setting.settingsList) {
            LabeledSwitch row = new LabeledSwitch(setting, scale);
            vbox.getChildren().add(row);
        }

        vbox.setStyle(
                "-fx-background-color: #EDE7F7;" +
                        "-fx-background-radius: " + (18 * scale) + ";" +
                        "-fx-border-radius: " + (18 * scale) + ";" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0.2, 0, 4);"
        );

        return vbox;
    }
}
