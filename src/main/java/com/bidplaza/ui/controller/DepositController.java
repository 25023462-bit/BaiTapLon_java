package com.bidplaza.ui.controller;

import com.bidplaza.model.user.Bidder;
import com.bidplaza.model.user.User;
import com.bidplaza.network.DepositRequest;
import com.bidplaza.network.Message;
import com.bidplaza.ui.AppStyles;
import com.bidplaza.ui.model.UserSession;
import com.bidplaza.ui.net.ServerClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class DepositController implements Initializable {

    @FXML private Label balanceLabel;
    @FXML private TextField amountField;
    @FXML private Label resultLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        javafx.application.Platform.runLater(() -> {
            if (amountField.getScene() != null) {
                AppStyles.applyTo(amountField.getScene());
            }
            updateBalanceLabel();
        });
    }

    @FXML
    private void handleDeposit() {
        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                showError("Số tiền phải lớn hơn 0");
                return;
            }
            String userId = UserSession.getInstance().getUserId();
            DepositRequest req = new DepositRequest(userId, amount);
            Message response = ServerClient.request(new Message(Message.Type.DEPOSIT, req));
            if (response.isSuccess()) {
                User user = UserSession.getCurrentUser();
                if (user instanceof Bidder bidder) {
                    bidder.setBalance((Double) response.getData());
                }
                updateBalanceLabel();
                amountField.clear();
                showSuccess("Nạp tiền thành công!");
            } else {
                showError(response.getMessage());
            }
        } catch (NumberFormatException e) {
            showError("Vui lòng nhập số hợp lệ");
        } catch (Exception e) {
            showError("Lỗi: " + e.getMessage());
        }
    }

    private void updateBalanceLabel() {
        User user = UserSession.getCurrentUser();
        if (user instanceof Bidder bidder) {
            balanceLabel.setText("Số dư: $" + String.format("%.2f", bidder.getBalance()));
        }
    }

    private void showSuccess(String msg) {
        resultLabel.setStyle("-fx-text-fill: #27ae60;");
        resultLabel.setText(msg);
    }

    private void showError(String msg) {
        resultLabel.setStyle("-fx-text-fill: #e74c3c;");
        resultLabel.setText(msg);
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/bidplaza/ui/BidderDashboard.fxml"));
            Scene scene = new Scene(loader.load());
            AppStyles.applyTo(scene);
            Stage stage = (Stage) balanceLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("BidPlaza - Bidder Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
