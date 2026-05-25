package com.bidplaza.ui.controller;

import com.bidplaza.network.LoginResponse;
import com.bidplaza.network.Message;
import com.bidplaza.ui.AppStyles;
import com.bidplaza.ui.net.ServerClient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> roleCombo;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {

        roleCombo.setItems(
                FXCollections.observableArrayList(
                        "BIDDER",
                        "SELLER",
                        "ADMIN"
                )
        );

        roleCombo.setValue("BIDDER");

        javafx.application.Platform.runLater(() -> {
            try {
                Scene scene = usernameField.getScene();
                if (scene != null) {
                    scene.getStylesheets().add(
                        getClass().getResource("/com/bidplaza/ui/style.css").toExternalForm()
                    );
                }
            } catch (Exception ignored) {}
        });
    }

    @FXML
    private void handleRegister() {

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleCombo.getValue();

        if (username.isEmpty() || password.isEmpty()) {

            showError("Please fill all fields.");
            return;
        }

        if (password.length() < 3) {

            showError("Password must be at least 3 characters.");
            return;
        }

        try {

            Message request =
                    Message.login(username, password, role, true);

            Message response =
                    ServerClient.request(request);

            LoginResponse loginResponse;

            if (response.getPayload() instanceof LoginResponse lr) {

                loginResponse = lr;

            } else {

                loginResponse = new LoginResponse(
                        response.getAmount() == 1,
                        response.getInfo(),
                        null
                );
            }

            if (loginResponse.isSuccess()) {

                showSuccess("Account created successfully!");

                goBackToLogin();

            } else {

                showError(loginResponse.getMessage());
            }

        } catch (Exception e) {

            showError("Register failed: " + e.getMessage());
        }
    }

    @FXML
    private void goBackToLogin() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/bidplaza/ui/Login.fxml")
            );

            Scene scene = new Scene(loader.load());

            AppStyles.applyTo(scene);

            Stage stage =
                    (Stage) usernameField.getScene().getWindow();

            stage.setScene(scene);

            stage.setTitle("BidPlaza - Login");

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void showSuccess(String message) {

        messageLabel.setStyle("-fx-text-fill: green;");
        messageLabel.setText(message);
    }

    private void showError(String message) {

        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText(message);
    }
}