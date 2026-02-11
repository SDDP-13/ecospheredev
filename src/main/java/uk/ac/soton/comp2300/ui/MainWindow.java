package uk.ac.soton.comp2300.ui;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp2300.App;
import uk.ac.soton.comp2300.scene.BaseScene;

public class MainWindow {
    private static final Logger logger = LogManager.getLogger(MainWindow.class);
    private final int width;
    private final int height;
    private final Stage stage;
    private BaseScene currentScene;
    private Scene scene;

    public MainWindow(Stage stage, int width, int height) {
        this.width = width;
        this.height = height;
        this.stage = stage;

        setupStage();
        setupResources();
        setupDefaultScene();
    }

    public void setupStage() {
        stage.setTitle("Ecosphere");
        stage.setMinWidth(width);
        stage.setMinHeight(height + 20);

        // This now calls the standard stop() method to ensure Michael's logic shuts down
        stage.setOnCloseRequest(ev -> App.getInstance().stop());
    }

    private void setupResources() {
        logger.info("Loading resources");
    }

    public void loadScene(BaseScene newScene) {
        cleanup();

        newScene.build();
        currentScene = newScene;
        scene = newScene.setScene();
        stage.setScene(scene);

        Platform.runLater(() -> currentScene.initialise());
    }

    public void setupDefaultScene() {
        this.scene = new Scene(new Pane(), width, height, Color.BLACK);
        stage.setScene(this.scene);
    }

    public void cleanup() {
        logger.info("Clearing up previous scene");
    }

    public Scene getScene() {
        return this.scene;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}