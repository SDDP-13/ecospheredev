package uk.ac.soton.comp2300.component;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class ToggleSwitch extends StackPane {

    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    private double travel = 10;
    private float height = 20;
    private float width = 40;

    private final Rectangle background;
    private final Circle knob;

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

        getChildren().addAll(background, knob);

        setOnMouseClicked(e -> setSelected(!isSelected()));

        selected.addListener((obs, oldV, newV) -> update());

        update();
    }

    private void update() {
        if (isSelected()) {
            background.setFill(Color.LIGHTGREEN);
            knob.setTranslateX(travel);
        } else {
            background.setFill(Color.LIGHTGRAY);
            knob.setTranslateX(-travel);
        }
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean value) {
        selected.set(value);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }
}
