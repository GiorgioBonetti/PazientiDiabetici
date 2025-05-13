package it.univr.pazientidiabetici;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // prende il file fxml e lo carica
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        // componente principale della applicazione
        Scene scene = new Scene(fxmlLoader.load());

        scene.getStylesheets().add(BootstrapFX.bootstrapFXStylesheet()); // Carica il foglio di stile

        // stage è la finestra principale
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}