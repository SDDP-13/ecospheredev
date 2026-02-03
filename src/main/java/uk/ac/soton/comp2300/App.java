package uk.ac.soton.comp2300;

import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp2300.scene.LoginScene;
import uk.ac.soton.comp2300.scene.MenuScene;
import uk.ac.soton.comp2300.ui.MainWindow;

/**
 * The main entry point for the SDDP 13 App.
 */
public class App extends Application {
    private static final Logger logger = LogManager.getLogger(App.class);

    // Simulated mobile device dimensions
    private final int width = 450;
    private final int height = 800;

    private static App instance;
    private Stage stage;

    public static void main(String[] args) {
        logger.info("Starting client");
        launch();
    }

    @Override
    public void start(Stage stage) {
        instance = this;
        this.stage = stage;
        open();
    }

    /**
     * Initializes the main window and sets the starting scene to Login.
     */
    public void open() {
        logger.info("Opening window at " + width + "x" + height);

        // Create the main window container
        var mainWindow = new MainWindow(stage, width, height);

        // Launch the LoginScene as the first screen the user sees
        // change between MenuScene and LoginScene for which you want to appear first
        mainWindow.loadScene(new MenuScene(mainWindow));

        stage.show();
    }

    public void shutdown() {
        logger.info("Shutting down");
        System.exit(0);
    }

    public static App getInstance() {
        return instance;
    }
}