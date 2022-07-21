module net.core.coremusic {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires org.jetbrains.annotations;
    requires java.sql;
    requires java.desktop;
    requires javafx.swing;

    opens net.core.coremusic to javafx.fxml;
    exports net.core.coremusic;
    exports net.core.coremusic.model;
}