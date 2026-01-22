package it.univr.diabete;

import it.univr.diabete.controller.MainShellController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    private static Stage primaryStage;
    private static MainShellController mainShellController;

    public static void setMainShellController(MainShellController controller) {
        mainShellController = controller;
    }

    public static MainShellController getMainShellController() {
        return mainShellController;
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        showLogin();
    }

    /** Mostra la schermata di login sullo stesso Stage principale. */
    public static void showLogin() {
        try {
            FXMLLoader loader =
                    new FXMLLoader(MainApp.class.getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1150, 850);
            scene.getStylesheets().add(
                    MainApp.class.getResource("/css/app.css").toExternalForm()
            );

            primaryStage.setTitle("Telemedicina - Gestione Diabete Tipo 2");
            primaryStage.setScene(scene);
            primaryStage.setWidth(1150);
            primaryStage.setHeight(850);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Mostra la MainShell (paziente o diabetologo) sullo stesso Stage. */
    public static void showMainShell(String role, String displayName) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(MainApp.class.getResource("/fxml/MainShell.fxml"));
            Parent root = loader.load();

            MainShellController shell = loader.getController();
            setMainShellController(shell);
            shell.setUserData(role, displayName); // se ti serve

            Scene scene = new Scene(root, 1150, 850);
            scene.getStylesheets().add(
                    MainApp.class.getResource("/css/app.css").toExternalForm()
            );

            primaryStage.setTitle("Telemedicina - Gestione Diabete Tipo 2");
            primaryStage.setScene(scene);
            primaryStage.setWidth(1150);
            primaryStage.setHeight(850);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}