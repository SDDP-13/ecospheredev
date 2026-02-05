package uk.ac.soton.comp2300.component;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;


public class ToggleSwitch extends StackPane {
    private BooleanProperty isEnable = new SimpleBooleanProperty(false);
    double travel = 10;
    float height = 20;
    float width = 40;

    Rectangle background;
    Circle knob;

    public ToggleSwitch() {
        this(1.0f);
    }

    public ToggleSwitch(float scale) {
        height *= scale;
        width *= scale;
        travel *= scale;
        
        background = new Rectangle(width, height);
        knob = new Circle(8 * scale);

        background.setArcHeight(height);
        background.setArcWidth(height);

        knob.setTranslateX(travel);;

        getChildren().addAll(background, knob);

        update();

        setOnMouseClicked(e -> isEnable.set(!isEnable.get()));
        isEnable.addListener((obs, oldV, newV) -> update());
    }

    private void update() {
        if (isEnable.get()) {
            background.setFill(Color.LIGHTGREEN);
            knob.setTranslateX(travel);
        } else {
            background.setFill(Color.LIGHTGRAY);
            knob.setTranslateX(-travel);
        }
    }

    public BooleanProperty isEnableProperty() {
        return isEnable;
    }
}
