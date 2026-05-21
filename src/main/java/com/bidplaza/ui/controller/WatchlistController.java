package com.bidplaza.ui.controller;

import com.bidplaza.ui.AppStyles;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.layout.BorderPane;

import javafx.stage.Stage;

public class WatchlistController {

    @FXML
    private BorderPane rootPane;

    @FXML
    public void initialize() {

        // FORCE CSS
        rootPane.setStyle(
                "-fx-font-family: 'Segoe UI';"
        );
    }

    @FXML
    private void goBack() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/bidplaza/ui/BidderDashboard.fxml"
                    )
            );

            Parent root = loader.load();

            Scene scene = new Scene(root, 1500, 850);

            AppStyles.applyTo(scene);

            Stage stage =
                    (Stage) rootPane.getScene().getWindow();

            stage.setScene(scene);

            stage.setTitle("BidPlaza - Dashboard");

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}