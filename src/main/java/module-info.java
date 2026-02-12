module uk.ac.soton.comp2211 {
    requires java.scripting;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires org.apache.logging.log4j;
    requires javafx.graphics;
    requires com.google.gson;
    opens uk.ac.soton.comp2300.ui to javafx.fxml;
    exports uk.ac.soton.comp2300;
    exports uk.ac.soton.comp2300.ui;
    exports uk.ac.soton.comp2300.scene;
    exports uk.ac.soton.comp2300.event;
    exports uk.ac.soton.comp2300.component;
    exports uk.ac.soton.comp2300.model;
    opens uk.ac.soton.comp2300.component to javafx.fxml;
    opens uk.ac.soton.comp2300.model to com.google.gson;
}