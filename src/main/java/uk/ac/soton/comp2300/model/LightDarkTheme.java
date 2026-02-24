package uk.ac.soton.comp2300.model;

import javafx.scene.Scene;
import java.util.Objects;

public class LightDarkTheme {
    private LightDarkTheme(){}

    private static final String Light = "/style/lightMode.css";
    private static final String Dark = "/style/darkMode.css";

    public static void applyTheme (Scene scene, boolean dark) {
        scene.getStylesheets().removeIf(x -> x.endsWith("lightMode.css") || x.endsWith("darkMode.css"));

        String newTheme;

        if (dark) {
            newTheme = Dark;
        } else {
            newTheme = Light;
        }
        scene.getStylesheets().add(Objects.requireNonNull(LightDarkTheme.class.getResource(newTheme)).toExternalForm());
    }


}
