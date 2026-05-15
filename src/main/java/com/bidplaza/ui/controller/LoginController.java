package com.bidplaza.ui.controller;

import com.bidplaza.ui.model.UserSession;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller cho màn hình Login
 */
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

        // Danh sách role
        roleCombo.setItems(
                FXCollections.observableArrayList(
                        "BIDDER",
                        "SELLER",
                        "ADMIN"
                )
        );

        // Role mặc định
        roleCombo.setValue("BIDDER");
    }

    @FXML
    private void handleLogin() {

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleCombo.getValue();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {

            errorLabel.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        if (password.length() < 3) {

            errorLabel.setText("Mật khẩu tối thiểu 3 ký tự!");
            return;
        }

        // Lưu session
        UserSession.getInstance().login(username, role);

        try {

            String fxml;

            switch (role) {

                case "SELLER":
                    fxml = "SellerDashboard.fxml";
                    break;

                case "ADMIN":
                    fxml = "AdminDashboard.fxml";
                    break;

                default:
                    fxml = "AuctionList.fxml";
                    break;
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/bidplaza/ui/" + fxml)
            );

            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) usernameField.getScene().getWindow();

            stage.setTitle("BidPlaza - " + role);

            // Fullscreen đẹp hơn
            stage.setMaximized(true);

            stage.setScene(scene);

            stage.show();

        } catch (Exception e) {

            errorLabel.setText("Lỗi chuyển màn hình!");

            e.printStackTrace();
        }
    }
}