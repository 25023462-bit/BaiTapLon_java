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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Fix Phase 1.4:
 * - BUG CŨ: khi server crash, chỉ hiện text lỗi nhỏ → người dùng không thấy rõ.
 * - FIX: showError() hiện Alert dialog nổi bật, đồng thời set errorLabel.
 * - BUG CŨ: UserSession chỉ lưu username + role, không lưu userId từ server.
 * - FIX: sau login thành công, gọi UserSession.login() với đầy đủ
 *   username, role, userId (lấy từ LoginResponse.getUser()).
 * - THÊM: disable nút Login khi đang chờ server để tránh double-submit.
 */
public class LoginController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label errorLabel;
    @FXML private javafx.scene.control.Button loginButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        roleCombo.setItems(FXCollections.observableArrayList("BIDDER", "SELLER", "ADMIN"));
        roleCombo.setValue("BIDDER");
        errorLabel.setText("");

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
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role     = roleCombo.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui long nhap day du thong tin!");
            return;
        }

        // Disable để tránh double-submit
        if (loginButton != null) loginButton.setDisable(true);

        try {
            sendLoginRequest(username, password, role);
        } finally {
            if (loginButton != null) loginButton.setDisable(false);
        }
    }

    @FXML
    private void openRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/bidplaza/ui/Register.fxml")
            );
            Scene scene = new Scene(loader.load());
            AppStyles.applyTo(scene);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("BidPlaza - Register");
            stage.show();
        } catch (Exception e) {
            showError("Khong mo duoc Register!");
        }
    }

    private void sendLoginRequest(String username, String password, String role) {
        try {
            Message request  = Message.login(username, password, role, false);
            Message response = ServerClient.request(request);

            LoginResponse loginResponse = extractLoginResponse(response);

            if (loginResponse != null && loginResponse.isSuccess()) {

                // Lấy role thực tế từ server (tránh client tự chọn role)
                String resolvedRole = (loginResponse.getUser() != null)
                    ? loginResponse.getUser().getRole()
                    : role;
                if (!resolvedRole.equalsIgnoreCase(role)) {
                    showError("Vai trò không khớp với tài khoản!");
                    return;
                }

                // Lấy userId thực từ server để dùng khi đặt giá
                String userId = (loginResponse.getUser() != null)
                    ? loginResponse.getUser().getId()
                    : null;

                // Set session đầy đủ
                com.bidplaza.model.user.User user = loginResponse.getUser();
                if (user != null) {
                    UserSession.setCurrentUser(user);
                } else {
                    UserSession.getInstance().login(username, resolvedRole, userId);
                }

                chuyenManHinh(resolvedRole);

            } else {
                String msg = (loginResponse != null)
                    ? loginResponse.getMessage()
                    : response.getInfo();
                showError(msg != null ? msg : "Loi khong xac dinh");
            }

        } catch (java.net.ConnectException e) {
            // Server chưa khởi động
            showErrorDialog("Khong the ket noi Server!",
                "Server chua khoi dong hoac da crash.\nVui long chay AuctionServer roi thu lai.");

        } catch (java.io.IOException e) {
            // Hết số lần reconnect (từ ServerClient)
            showErrorDialog("Mat ket noi Server", e.getMessage());

        } catch (Exception e) {
            showError("Loi: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private LoginResponse extractLoginResponse(Message response) {
        if (response.getPayload() instanceof LoginResponse loginResponse) {
            return loginResponse;
        }
        return new LoginResponse(response.getAmount() == 1, response.getInfo(), null);
    }

    private void chuyenManHinh(String role) {
        try {
            String fxml;
            if ("SELLER".equals(role)) {
                fxml = "SellerDashboard.fxml";
            } else if ("ADMIN".equals(role)) {
                fxml = "AdminDashboard.fxml";
            } else {
                fxml = "BidderDashboard.fxml";
            }

            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/bidplaza/ui/" + fxml)
            );
            Scene scene = new Scene(loader.load());
            AppStyles.applyTo(scene);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("BidPlaza - " + role);
            stage.setMaximized(true);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            showError("Loi chuyen man hinh: " + e.getMessage());
        }
    }

    /** Hiện lỗi nhỏ dưới form (vẫn giữ để tương thích) */
    private void showError(String message) {
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        errorLabel.setText(message);
    }

    /** Hiện Alert dialog nổi bật khi server crash hoặc lỗi nghiêm trọng */
    private void showErrorDialog(String header, String content) {
        showError(header); // vẫn set errorLabel
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Loi ket noi");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
