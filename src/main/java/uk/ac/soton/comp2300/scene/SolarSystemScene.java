package uk.ac.soton.comp2300.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uk.ac.soton.comp2300.ui.MainPane;
import uk.ac.soton.comp2300.ui.MainWindow;

public class SolarSystemScene extends BaseScene {
    public SolarSystemScene(MainWindow mainWindow) { super(mainWindow); }

    @Override
    public void build() {
        root = new MainPane(mainWindow.getWidth(), mainWindow.getHeight());
        root.getStyleClass().add("root-black");

        // Back button to return to the planet
        Button btnBack = new Button("⬅");
        btnBack.setPrefSize(50, 50);
        btnBack.getStyleClass().add("menu-icon-button");
        btnBack.setOnAction(e -> mainWindow.loadScene(new MenuScene(mainWindow)));

        StackPane.setAlignment(btnBack, Pos.TOP_LEFT);
        StackPane.setMargin(btnBack, new Insets(20));

        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        Label title = new Label("Solar System View");
        title.getStyleClass().add("title-xlarge");

        container.getChildren().add(title);
        root.getChildren().addAll(container, btnBack);
    }

    @Override public void initialise() {}
}