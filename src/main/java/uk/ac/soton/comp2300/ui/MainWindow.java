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
import uk.ac.soton.comp2300.scene.DashboardScene;
import uk.ac.soton.comp2300.scene.MenuScene;

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

        // Setup window
        setupStage();

        // Setup resources
        setupResources();

        // Setup default scene
        setupDefaultScene();

        // Go to intro scene
        this.loadScene(new MenuScene(this));
    }

    public void setupStage() {
        stage.setTitle("Ecosphere");
        stage.setMinWidth(width);
        stage.setMinHeight(height + 20);
        stage.setOnCloseRequest(ev -> App.getInstance().shutdown());
    }

    private void setupResources() {
        logger.info("Loading resources");

        // No resources yet
    }

    public void loadScene(BaseScene newScene) {
        //Cleanup remains of the previous scene
        cleanup();

        //Create the new scene and set it up
        newScene.build();
        currentScene = newScene;
        scene = newScene.setScene();
        stage.setScene(scene);

        //Initialise the scene when ready
        Platform.runLater(() -> currentScene.initialise());
    }

    /**
     * Setup the default scene (an empty black scene) when no scene is loaded
     */
    public void setupDefaultScene() {
        this.scene = new Scene(new Pane(), width, height, Color.BLACK);
        stage.setScene(this.scene);
    }

    /**
     * When switching scenes, perform any cleanup needed, such as removing previous listeners
     */
    public void cleanup() {
        logger.info("Clearing up previous scene");

        // No cleanup necessary yet
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
