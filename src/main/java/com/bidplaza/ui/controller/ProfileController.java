package com.bidplaza.ui.controller;

import com.bidplaza.ui.AppStyles;
import com.bidplaza.ui.model.UserSession;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;

import javafx.scene.control.Label;

import javafx.scene.layout.BorderPane;

import javafx.stage.Stage;

public class ProfileController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label emailLabel;

    @FXML
    public void initialize() {

        String username =
                UserSession.getInstance().getUsername();

        String role =
                UserSession.getInstance().getRole();

        usernameLabel.setText(username);

        roleLabel.setText(role);

        // EMAIL GIẢ LẬP

        emailLabel.setText(
                username.toLowerCase() + "@bidplaza.com"
        );

        javafx.application.Platform.runLater(() -> {
            try {
                Scene scene = emailLabel.getScene();
                if (scene != null) {
                    scene.getStylesheets().add(
                        getClass().getResource("/com/bidplaza/ui/style.css").toExternalForm()
                    );
                }
            } catch (Exception ignored) {}
        });
    }

    @FXML
    private void goBack() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/bidplaza/ui/BidderDashboard.fxml"
                    )
            );

            Scene scene = new Scene(loader.load());

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