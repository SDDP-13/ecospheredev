package uk.ac.soton.comp2300;

import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp2300.ui.MainWindow;

public class App extends Application {
    private static final Logger logger = LogManager.getLogger(App.class);
    private final int width = 800;
    private final int height = 600;

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

    public void open() {
        logger.info("Opening window");

        var mainWindow = new MainWindow(stage, width, height);

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
