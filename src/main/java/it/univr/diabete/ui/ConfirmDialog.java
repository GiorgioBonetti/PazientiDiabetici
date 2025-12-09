package it.univr.diabete.ui;

import it.univr.diabete.controller.ConfirmDialogController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConfirmDialog {

    public static boolean show(String title, String message, String subtitle) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    ConfirmDialog.class.getResource("/fxml/ConfirmDialogView.fxml")
            );

            Parent root = loader.load();
            ConfirmDialogController ctrl = loader.getController();
            ctrl.setTexts(title, message + "\n" + (subtitle != null ? subtitle : ""));

            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setScene(new Scene(root));
            popup.setResizable(false);
            popup.showAndWait();

            return ctrl.isConfirmed();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}