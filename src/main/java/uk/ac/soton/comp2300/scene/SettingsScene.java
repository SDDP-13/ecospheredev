package uk.ac.soton.comp2300.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;

import uk.ac.soton.comp2300.component.ToggleSwitch;
import uk.ac.soton.comp2300.model.Setting;
import uk.ac.soton.comp2300.model.Setting.SettingOption;

/**
 * Build the settings menu UI
 */
public class SettingsScene extends BaseScene {
    private static final Logger logger = LogManager.getLogger(SettingsScene.class);
    private static final double BASE = 44.0;
    private static final double SCALE = BASE / 44.0;

    public SettingsScene(MainWindow mainWindow) {
        super(mainWindow);
    }

@Override
public void build() {
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

    // Navigation logic to return to Login
    btnLogout.setOnAction(e -> mainWindow.loadScene(new LoginScene(mainWindow)));

    VBox container = new VBox(BASE * 0.35, settingsBox, btnAccountDetails, btnLogout, btnDelete);
    container.setAlignment(Pos.TOP_CENTER);
    container.setPadding(new Insets(0, BASE * 0.45, BASE * 0.45, BASE * 0.45));
    container.setMaxWidth(420);

    VBox screen = new VBox(0, topBar, container);
    screen.setAlignment(Pos.TOP_CENTER);

    root.getChildren().add(screen);
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

    public ToggleSwitch getToggle() {
      return toggle;
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



  @Override
  public void initialise() {
    // Nothing to initialise.
  }
}
