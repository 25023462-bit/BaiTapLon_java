package com.bidplaza.ui.controller;

import com.bidplaza.model.user.Bidder;
import com.bidplaza.model.user.User;
import com.bidplaza.network.DepositRequest;
import com.bidplaza.network.Message;
import com.bidplaza.ui.AppStyles;
import com.bidplaza.ui.model.UserSession;
import com.bidplaza.ui.net.ServerClient;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ResourceBundle;

public class DepositController implements Initializable {

    private final DecimalFormat moneyFormat = new DecimalFormat("#,###");

    @FXML
    private Label balanceLabel;

    @FXML
    private TextField amountField;

    @FXML
    private Label resultLabel;

    @FXML
    private VBox depositPane;

    @FXML
    private VBox invoicePane;

    @FXML
    private Label invoiceUserLabel;

    @FXML
    private Label invoiceBalanceLabel;

    @FXML
    private Label invoiceAmountLabel;

    @FXML
    private Label invoiceTotalLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Platform.runLater(() -> {

            if (amountField.getScene() != null) {
                AppStyles.applyTo(amountField.getScene());
            }

            updateBalanceLabel();

            invoicePane.setVisible(false);
            invoicePane.setManaged(false);

            depositPane.setVisible(true);
            depositPane.setManaged(true);
        });

        // KHÔNG CHO NHẬP CHỮ
        amountField.textProperty().addListener((observable, oldValue, newValue) -> {

            if (!newValue.matches("\\d*")) {

                amountField.setText(newValue.replaceAll("[^\\d]", ""));

                showError("❌ Chỉ được nhập số!");
            }
        });
    }

    /**
     * HIỂN THỊ HÓA ĐƠN
     */
    @FXML
    private void showInvoice() {

        try {

            String text = amountField.getText().trim();

            if (text.isEmpty()) {
                showError("❌ Vui lòng nhập số tiền");
                return;
            }

            double amount = Double.parseDouble(text);

            if (amount <= 0) {
                showError("❌ Số tiền phải lớn hơn 0");
                return;
            }

            User user = UserSession.getCurrentUser();

            double currentBalance = 0;

            if (user instanceof Bidder bidder) {
                currentBalance = bidder.getBalance();
            }

            invoiceUserLabel.setText(
                    "👤 Người dùng: "
                            + UserSession.getInstance().getUsername());

            invoiceBalanceLabel.setText(
                    "💰 Số dư hiện tại: "
                            + moneyFormat.format(currentBalance)
                            + " VNĐ");

            invoiceAmountLabel.setText(
                    "➕ Số tiền nạp: "
                            + moneyFormat.format(amount)
                            + " VNĐ");

            invoiceTotalLabel.setText(
                    "🟢 Tổng sau nạp: "
                            + moneyFormat.format(currentBalance + amount)
                            + " VNĐ");

            depositPane.setVisible(false);
            depositPane.setManaged(false);

            invoicePane.setVisible(true);
            invoicePane.setManaged(true);

            resultLabel.setText("");

        } catch (NumberFormatException e) {

            showError("❌ Vui lòng nhập số hợp lệ");
        }
    }

    /**
     * XÁC NHẬN GIAO DỊCH
     */
    @FXML
    private void handleDeposit() {

        try {

            String text = amountField.getText().trim();

            if (text.isEmpty()) {
                showError("❌ Vui lòng nhập số tiền");
                return;
            }

            double amount = Double.parseDouble(text);

            if (amount <= 0) {
                showError("❌ Số tiền phải lớn hơn 0");
                return;
            }

            String userId = UserSession.getInstance().getUserId();

            DepositRequest request =
                    new DepositRequest(userId, amount);

            Message response = ServerClient.request(
                    new Message(Message.Type.DEPOSIT, request));

            if (response.isSuccess()) {

                User user = UserSession.getCurrentUser();

                if (user instanceof Bidder bidder) {
                    bidder.setBalance((Double) response.getData());
                }

                updateBalanceLabel();

                amountField.clear();

                showSuccess("✅ NẠP TIỀN THÀNH CÔNG!");

                invoicePane.setVisible(false);
                invoicePane.setManaged(false);

                depositPane.setVisible(true);
                depositPane.setManaged(true);

            } else {

                showError(response.getMessage());
            }

        } catch (NumberFormatException e) {

            showError("❌ Vui lòng nhập số hợp lệ");

        } catch (Exception e) {

            showError("❌ Lỗi: " + e.getMessage());
        }
    }

    /**
     * HỦY HÓA ĐƠN
     */
    @FXML
    private void hideInvoice() {

        invoicePane.setVisible(false);
        invoicePane.setManaged(false);

        depositPane.setVisible(true);
        depositPane.setManaged(true);

        resultLabel.setText("");
    }

    /**
     * UPDATE BALANCE
     */
    private void updateBalanceLabel() {

        User user = UserSession.getCurrentUser();

        if (user instanceof Bidder bidder) {

            balanceLabel.setText(
                    moneyFormat.format(bidder.getBalance())
                            + " VNĐ");
        }
    }

    /**
     * SUCCESS
     */
    private void showSuccess(String msg) {

        resultLabel.setStyle(
                "-fx-text-fill: #00ff99;" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;");

        resultLabel.setText(msg);
    }

    /**
     * ERROR
     */
    private void showError(String msg) {

        resultLabel.setStyle(
                "-fx-text-fill: #ff4d6d;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;");

        resultLabel.setText(msg);
    }

    /**
     * BACK
     */
    @FXML
    private void handleBack() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/bidplaza/ui/BidderDashboard.fxml"));

            Scene scene = new Scene(loader.load());

            AppStyles.applyTo(scene);

            Stage stage =
                    (Stage) balanceLabel.getScene().getWindow();

            stage.setScene(scene);

            stage.setTitle("BidPlaza - Bidder Dashboard");

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}