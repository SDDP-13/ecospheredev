package uk.ac.soton.comp2211.scene;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp2211.component.Hello;
import uk.ac.soton.comp2211.ui.MainPane;
import uk.ac.soton.comp2211.ui.MainWindow;

public class DashboardScene extends BaseScene {
    private static final Logger logger = LogManager.getLogger(DashboardScene.class);

    public DashboardScene(MainWindow mainWindow) {
        super(mainWindow);
    }

    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());

        var stackPane = new StackPane();
        stackPane.setMaxWidth(mainWindow.getWidth());
        stackPane.setMaxHeight(mainWindow.getHeight());
        stackPane.getStyleClass().add("black");
        root.getChildren().add(stackPane);

        var borderPane = new BorderPane();
        stackPane.getChildren().add(borderPane);

        var hello = new Hello();        // single instance
        borderPane.setCenter(hello);    // attach it to the scene
    }

    @Override
    public void initialise() {
        // Nothing to initialise. Helpful for key-presses if we need it.
    }
}
