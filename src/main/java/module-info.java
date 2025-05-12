module it.univr.pazientidiabetici {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens it.univr.pazientidiabetici to javafx.fxml;
    exports it.univr.pazientidiabetici;
}