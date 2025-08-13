module com.example.pazienti {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.example.pazienti to javafx.fxml;
    exports com.example.pazienti;
    exports com.example.pazienti.controller;
    opens com.example.pazienti.controller to javafx.fxml;
}