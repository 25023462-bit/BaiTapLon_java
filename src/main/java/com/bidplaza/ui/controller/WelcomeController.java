package com.bidplaza.ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class WelcomeController {

    public void goToLogin(ActionEvent event) {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource("/com/bidplaza/ui/Login.fxml")
            );

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource())
                    .getScene()
                    .getWindow();

            Scene scene = new Scene(root);

            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}