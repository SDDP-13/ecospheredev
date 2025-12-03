module uk.ac.soton.comp2211 {
    requires java.scripting;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires org.apache.logging.log4j;
    opens uk.ac.soton.comp2211.ui to javafx.fxml;
    exports uk.ac.soton.comp2211;
    exports uk.ac.soton.comp2211.ui;
    exports uk.ac.soton.comp2211.scene;
    exports uk.ac.soton.comp2211.event;
    exports uk.ac.soton.comp2211.component;
    exports uk.ac.soton.comp2211.model;
    opens uk.ac.soton.comp2211.component to javafx.fxml;
}