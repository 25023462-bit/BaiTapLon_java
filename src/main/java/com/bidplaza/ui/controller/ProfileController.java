package com.bidplaza.ui.controller;

import com.bidplaza.network.Message;
import com.bidplaza.network.ProfileData;
import com.bidplaza.ui.AppStyles;
import com.bidplaza.ui.model.UserSession;
import com.bidplaza.ui.net.ServerClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ProfileController {

    @FXML private BorderPane rootPane;
    @FXML private Label usernameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private Label statsTitle;
    @FXML private Label stat1Label;
    @FXML private Label stat2Label;
    @FXML private Label stat3Label;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private Label passwordResultLabel;

    @FXML
    public void initialize() {
    new Thread(() -> loadProfile(), "profile-loader").start();
    Platform.runLater(() -> {
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

    private void loadProfile() {
    new Thread(() -> {
        try {
            Message response = ServerClient.request(new Message(
                Message.Type.GET_PROFILE, null,
                UserSession.getInstance().getUserId(), 0, null));
            Platform.runLater(() -> {
                if (response.getPayload() instanceof ProfileData profile) {
                    usernameLabel.setText(profile.getUsername());
                    emailLabel.setText(profile.getEmail());
                    roleLabel.setText(profile.getRole());
                    updateStats(profile);
                } else {
                    showPasswordResult("Khong tai duoc profile", false);
                }
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                usernameLabel.setText(UserSession.getInstance().getUsername());
                roleLabel.setText(UserSession.getInstance().getRole());
                emailLabel.setText("");
                showPasswordResult("Khong ket noi duoc server: " + e.getMessage(), false);
            });
        }
    }, "profile-loader").start();
}
    private void updateStats(ProfileData profile) {
        if ("SELLER".equals(profile.getRole())) {
            statsTitle.setText("Thong ke Seller");
            stat1Label.setText("Phien da tao: " + profile.getTotalAuctionsCreated());
            stat2Label.setText("Phien da ban: " + profile.getTotalAuctionsSold());
            stat3Label.setText("");
            return;
        }

        statsTitle.setText("Thong ke Bidder");
        stat1Label.setText("So du: $" + String.format("%.2f", profile.getBalance()));
        stat2Label.setText("Tong bid da dat: " + profile.getTotalBidsPlaced());
        stat3Label.setText("Phien da thang: " + profile.getTotalAuctionsWon());
    }

    @FXML
    private void handleChangePassword() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        if (oldPassword == null || oldPassword.isBlank()
                || newPassword == null || newPassword.isBlank()) {
            showPasswordResult("Nhap day du mat khau cu va moi.", false);
            return;
        }

        try {
            Message response = ServerClient.request(new Message(
                Message.Type.UPDATE_PASSWORD, null,
                UserSession.getInstance().getUserId(), 0,
                oldPassword + ":" + newPassword));
            if ("SUCCESS".equals(response.getInfo())) {
                oldPasswordField.clear();
                newPasswordField.clear();
                showPasswordResult("Da doi mat khau.", true);
            } else {
                showPasswordResult("Mat khau cu khong dung.", false);
            }
        } catch (Exception e) {
            showPasswordResult("Khong doi duoc mat khau: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleBack() {
        try {
            String fxml = switch (String.valueOf(UserSession.getInstance().getRole())) {
                case "SELLER" -> "SellerDashboard.fxml";
                case "ADMIN" -> "AdminDashboard.fxml";
                default -> "BidderDashboard.fxml";
            };
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/bidplaza/ui/" + fxml));
            Scene scene = new Scene(loader.load());
            AppStyles.applyTo(scene);
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("BidPlaza - Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {
        handleBack();
    }

    private void showPasswordResult(String message, boolean success) {
        if (passwordResultLabel != null) {
            passwordResultLabel.setStyle(success
                ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
            passwordResultLabel.setText(message);
        }
    }
}
