package uk.ac.soton.comp2211.scene;

import javafx.scene.Scene;
import javafx.scene.paint.Color;
import uk.ac.soton.comp2211.ui.MainPane;
import uk.ac.soton.comp2211.ui.MainWindow;

public abstract class BaseScene  {
    protected final MainWindow mainWindow;
    protected MainPane root;
    protected Scene scene;

    public BaseScene(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    /**
     * Initialise this scene. Called after creation
     */
    public abstract void initialise();

    /**
     * Build the layout of the scene
     */
    public abstract void build();

    /**
     * Create a new JavaFX scene using the root contained within this scene
     * @return JavaFX scene
     */
    public Scene setScene() {
        var previous = mainWindow.getScene();
        Scene scene = new Scene(this.root, previous.getWidth(), previous.getHeight(), Color.BLACK);
        scene.getStylesheets().add(getClass().getResource("/style/main.css").toExternalForm());
        this.scene = scene;
        return scene;
    }

    /**
     * Get the JavaFX scene contained inside
     * @return JavaFX scene
     */
    public Scene getScene() {
        return this.scene;
    }
}