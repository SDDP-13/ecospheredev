package uk.ac.soton.comp2300.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Hello extends VBox {
    private static final Logger logger = LogManager.getLogger(Hello.class);

    public Hello() {
        logger.info("Creating " + this.getClass().getName());

        // Base settings
        this.setSpacing(8);
        this.setPadding(new Insets(8, 8, 8, 8));
        this.getStyleClass().add("helloBox");

        // Top text
        var helloTopText = new Text("SDDP Group 13 yahoo");
        helloTopText.getStyleClass().add("helloText");

        // GIF
        var imageView = new ImageView();
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        var image = new Image(getClass().getResource("/images/Programming.gif").toExternalForm());
        imageView.setImage(image);
        imageView.setOpacity(1);

        // Bottom text
        var helloBottomText = new Text("gl guys");
        helloBottomText.getStyleClass().add("helloText");

        // Layout
        this.setAlignment(Pos.CENTER);
        this.getChildren().addAll(helloTopText, imageView, helloBottomText);
    }
}
//test again
