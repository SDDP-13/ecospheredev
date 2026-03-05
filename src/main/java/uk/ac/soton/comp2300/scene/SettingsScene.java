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
import uk.ac.soton.comp2300.model.LightDarkTheme;
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

        for (SettingOption option: Setting.settingsList) {
            if (option.getKey().equals("darkMode")) {
                option.enabledProperty().addListener((obs, oldV, newV)
                        -> {LightDarkTheme.applyTheme(mainWindow.getStage().getScene(), newV);
                });

                LightDarkTheme.applyTheme(mainWindow.getStage().getScene(), option.enabledProperty().get());
                break;
            }
        }

        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.getStyleClass().add("root-light");

        Button btnBack = new Button("←");
        btnBack.setPrefSize(BASE, BASE);
        btnBack.getStyleClass().addAll("bg-transparent","title-large");
        btnBack.getStyleClass().add("menu-icon-button");
        btnBack.setOnAction(e -> mainWindow.loadScene(new MenuScene(mainWindow)));

        StackPane.setAlignment(btnBack, Pos.TOP_LEFT);
        StackPane.setMargin(btnBack, new Insets(20));

        Label title = new Label("Settings");
        title.getStyleClass().add("title-xlarge");

        Button blbGear = new Button("\u2699");
        blbGear.setPrefSize(BASE, BASE);
        blbGear.getStyleClass().addAll("title-large", "bg-transparent");

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

        Button btnAccountDetails = makeActionButton("Account Details", "button-primary");
        Button btnLogout = makeActionButton("Logout", "button-primary");
        Button btnDelete = makeActionButton("Delete Account", "button-secondary");

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
        dim.getStyleClass().addAll("overlay-dim");
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
        box.getStyleClass().addAll("button-shape-rounded-large", "effect-1", "label-empty");


        Label title = new Label("Account Details");
        title.getStyleClass().addAll("title-large-font");

        Label hint = new Label("Hard coded, password is `Pass1`");
        hint.getStyleClass().addAll("title-medium");

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
        styleSmallButton(close, "button-tertiary");
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
            getStyleClass().addAll("overlay-medium", "button-shape-rounded");

            VBox text = new VBox(3);

            Label l = new Label(label);
            l.getStyleClass().addAll("title-small-dark");


            Label v = new Label(value);
            v.getStyleClass().addAll("title-small-dark");


            text.getChildren().addAll(l, v);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button edit = new Button("Edit");
            edit.getStyleClass().addAll("button-tertiary","button-shape-rounded", "font-weight-1" );

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
            getStyleClass().addAll("button-shape-rounded-large", "effect-1", "root-light");

            Label title = new Label("Create New Username");
            title.getStyleClass().add("title-large");

            Label current = new Label("Current username: " + currentUsername);
            current.getStyleClass().add("title-small");

            TextField tf = new TextField();
            tf.setPromptText("Enter new username");
            tf.getStyleClass().addAll("root-light", "title-small");
            tf.setPrefHeight(40);

            Label status = new Label("Note: This is hard coded (does nothing yet).");
            status.getStyleClass().add("title-small");

            Button cancel = new Button("Cancel");
            styleSmallButton(cancel, "button-tertiary");
            cancel.setOnAction(e -> onCancel.run());

            Button confirm = new Button("Confirm");
            styleSmallButton(confirm, "button-secondary");

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
            getStyleClass().addAll("button-shape-round-large","button-tertiary","effect-1");

            Label title = new Label("Create New Password");
            title.getStyleClass().add("title-large");

            Label msg = new Label("Enter a new password and retype it.");
            //msg.getStyleClass().add("title-small");

            PasswordField pf1 = new PasswordField();
            pf1.setPromptText("New password");
            //pf1.getStyleClass().addAll("title-small", "root-light");
            pf1.setPrefHeight(40);

            PasswordField pf2 = new PasswordField();
            pf2.setPromptText("Retype new password");
            //pf2.getStyleClass().addAll("title-small", "root-light");
            pf2.setPrefHeight(40);

            Label status = new Label("Note: This is hard coded (does nothing yet).");
            //status.getStyleClass().add("title-small");

            Button cancel = new Button("Cancel");
            styleSmallButton(cancel, "button-tertiary");
            cancel.setOnAction(e -> onCancel.run());

            Button confirm = new Button("Confirm");
            styleSmallButton(confirm, "button-secondary");

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
        box.getStyleClass().addAll("button-shape-round-large","button-tertiary","effect-1");


        Label title = new Label("Confirm Deletion");
        title.getStyleClass().add("title-large");

        Label warning = new Label("This will permanently delete your account. (does nothing)");
        warning.getStyleClass().add("title-small");

        CheckBox understand = new CheckBox("I understand");
        understand.getStyleClass().add("title-small");

        Label status = new Label("Confirm will unlock in 5 seconds…");
        status.getStyleClass().add("title-small");

        Button cancel = new Button("Cancel");
        styleSmallButton(cancel, "button-tertiary");
        cancel.setOnAction(e -> clearOverlay());

        Button confirm = new Button("Confirm (5)");
        styleSmallButton(confirm, "button-secondary");
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
            getStyleClass().addAll("button-shape-round-large","button-tertiary","effect-1");

            Label title = new Label(titleText);
            title.getStyleClass().addAll("title-large");

            Label msg = new Label(messageText);
            msg.getStyleClass().add("label-small");


            PasswordField pf = new PasswordField();
            pf.setPromptText("Enter password");
            pf.getStyleClass().add("label-small");
            pf.setPrefHeight(40);

            Label status = new Label("");
            status.getStyleClass().add("label-small");

            Button cancel = new Button("Cancel");
            styleSmallButtonStatic(cancel, "button-tertiary");
            cancel.setOnAction(e -> onResult.accept(new Setting.PermissionResult(false, "Cancelled.")));

            Button confirm = new Button("Confirm");
            styleSmallButtonStatic(confirm, "button-secondary");

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

        private static void styleSmallButtonStatic(Button b, String buttonStyle) {
            b.setPrefWidth(140);
            b.setPrefHeight(40);
            b.getStyleClass().addAll(buttonStyle,"button-shape-rounded-small");

        }
    }

    private Button makeActionButton(String text, String buttonStyle) {
        Button b = new Button(text);
        b.setPrefWidth(BASE * 5);
        b.setPrefHeight(BASE * 1.35);
        b.getStyleClass().addAll("button-structure", buttonStyle, "button-shape-rounded", "padding");

        return b;
    }

    private void styleSmallButton(Button b, String buttonStyle) {
        b.setPrefWidth(140);
        b.setPrefHeight(40);
        b.getStyleClass().addAll("button-primary", "button-shape-rounded");

    }

    private static class LabeledSwitch extends HBox {
        private final ToggleSwitch toggle;

        public LabeledSwitch(SettingOption setting, float scale) {
            super(12 * scale);

            Label titleLabel = new Label(setting.getTitle());
            titleLabel.getStyleClass().addAll("title-medium-small", "font-weight-1");


            Label descLabel = new Label(setting.getDescription());
            descLabel.setWrapText(true);
            descLabel.getStyleClass().add("label-small");

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
        vbox.getStyleClass().addAll("button-shape-rounded-large","effect-2", "root-light");


        return vbox;
    }

}
