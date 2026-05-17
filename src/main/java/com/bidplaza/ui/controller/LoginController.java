package com.bidplaza.ui.controller;

import com.bidplaza.network.LoginResponse;
import com.bidplaza.network.Message;
import com.bidplaza.ui.AppStyles;
import com.bidplaza.ui.model.UserSession;
import com.bidplaza.ui.net.ServerClient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> roleCombo;

    @FXML
    private Label errorLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        roleCombo.setItems(
                FXCollections.observableArrayList(
                        "BIDDER",
                        "SELLER",
                        "ADMIN"
                )
        );

        roleCombo.setValue("BIDDER");
    }

    @FXML
    private void handleLogin() {

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleCombo.getValue();

        if (username.isEmpty() || password.isEmpty()) {

            showError("Vui long nhap day du thong tin!");
            return;
        }

        sendLoginRequest(username, password, role);
    }

    @FXML
    private void openRegister() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/bidplaza/ui/Register.fxml")
            );

            Scene scene = new Scene(loader.load());

            AppStyles.applyTo(scene);

            Stage stage =
                    (Stage) usernameField.getScene().getWindow();

            stage.setScene(scene);

            stage.setTitle("BidPlaza - Register");

            stage.show();

        } catch (Exception e) {

            showError("Khong mo duoc Register!");
        }
    }

    private void sendLoginRequest(String username,
                                  String password,
                                  String role) {

        try {

            Message request =
                    Message.login(username, password, role, false);

            Message response =
                    ServerClient.request(request);

            LoginResponse loginResponse =
                    extractLoginResponse(response);

            if (loginResponse != null
                    && loginResponse.isSuccess()) {

                String resolvedRole =
                        loginResponse.getUser() != null
                                ? loginResponse.getUser().getRole()
                                : role;

                UserSession.getInstance()
                        .login(username, resolvedRole);

                chuyenManHinh(resolvedRole);

            } else {

                String msg =
                        loginResponse != null
                                ? loginResponse.getMessage()
                                : response.getInfo();

                showError(
                        msg != null
                                ? msg
                                : "Loi khong xac dinh"
                );
            }

        } catch (java.net.ConnectException e) {

            showError("Khong the ket noi Server!");

        } catch (Exception e) {

            showError(
                    "Loi: "
                            + e.getClass().getSimpleName()
                            + " - "
                            + e.getMessage()
            );
        }
    }

    private LoginResponse extractLoginResponse(
            Message response) {

        if (response.getPayload()
                instanceof LoginResponse loginResponse) {

            return loginResponse;
        }

        return new LoginResponse(
                response.getAmount() == 1,
                response.getInfo(),
                null
        );
    }

    private void chuyenManHinh(String role) {

        try {

            String fxml;

            if (role.equals("SELLER")) {

                fxml = "SellerDashboard.fxml";

            } else if (role.equals("ADMIN")) {

                fxml = "AdminDashboard.fxml";

            } else {

                fxml = "BidderDashboard.fxml";
            }

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/com/bidplaza/ui/" + fxml
                            )
                    );

            Scene scene =
                    new Scene(loader.load());

            AppStyles.applyTo(scene);

            Stage stage =
                    (Stage) usernameField
                            .getScene()
                            .getWindow();

            stage.setTitle(
                    "BidPlaza - " + role
            );

            stage.setMaximized(true);

            stage.setScene(scene);

            stage.show();

        } catch (Exception e) {

            showError(
                    "Loi chuyen man hinh: "
                            + e.getMessage()
            );
        }
    }

    private void showError(String message) {

        errorLabel.setStyle(
                "-fx-text-fill: #e74c3c;"
        );

        errorLabel.setText(message);
    }
}