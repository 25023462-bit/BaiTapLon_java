package com.bidplaza.ui.controller;

import com.bidplaza.ui.AppStyles;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WelcomeController {

    @javafx.fxml.FXML
    private javafx.scene.layout.StackPane rootPane;

    @javafx.fxml.FXML
    public void initialize() {
        javafx.application.Platform.runLater(() -> {
            try {
                Scene scene = rootPane.getScene();
                if (scene != null) {
                    scene.getStylesheets().add(
                        getClass().getResource("/com/bidplaza/ui/style.css").toExternalForm()
                    );
                }
            } catch (Exception ignored) {}
        });
    }

    public void goToLogin(ActionEvent event) {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/bidplaza/ui/Login.fxml")
            );

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource())
                    .getScene()
                    .getWindow();

            Scene scene = new Scene(root);
            AppStyles.applyTo(scene);

            if (stage.getScene() != null) {
                javafx.scene.Parent rootNode = scene.getRoot();
                scene.setRoot(new javafx.scene.layout.Pane());
                stage.getScene().setRoot(rootNode);
            } else {
                stage.setScene(scene);
            }
            stage.setMaximized(true);
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void goToRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/bidplaza/ui/Register.fxml"));
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource())
                    .getScene().getWindow();
            Scene scene = new Scene(root);
            AppStyles.applyTo(scene);
            if (stage.getScene() != null) {
                javafx.scene.Parent rootNode = scene.getRoot();
                scene.setRoot(new javafx.scene.layout.Pane());
                stage.getScene().setRoot(rootNode);
            } else {
                stage.setScene(scene);
            }
            stage.setMaximized(true);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
