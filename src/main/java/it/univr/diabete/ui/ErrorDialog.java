package it.univr.diabete.ui;

import it.univr.diabete.MainApp;
import it.univr.diabete.controller.ErrorDialogController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ErrorDialog {

    public static void show(String title, String message) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApp.class.getResource("/fxml/ErrorDialogView.fxml"));
            Parent root = loader.load();

            ErrorDialogController ctrl = loader.getController();
            ctrl.setTexts(title, message);

            Stage popup = new Stage();
            popup.initModality(Modality.WINDOW_MODAL);
            popup.setScene(new Scene(root));
            popup.setTitle(title);
            popup.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
