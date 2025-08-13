package com.example.pazienti;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("AddSintomo.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Add Sintomo");
        stage.setScene(scene);
        stage.show();


    }
}
