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
 * Controller cho màn hình Login.
 *
 * Trong MVC:
 * - View: Login.fxml
 * - Controller: LoginController (file này)
 * - Model: UserSession (lưu thông tin user đang đăng nhập)
 */
public class LoginController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label errorLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Điền các vai trò vào ComboBox
        roleCombo.setItems(FXCollections.observableArrayList("BIDDER", "SELLER", "ADMIN"));
        roleCombo.setValue("BIDDER"); // mặc định
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role     = roleCombo.getValue();

        // Kiểm tra rỗng
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // TODO: sau này gọi Server qua Socket để xác thực
        // Hiện tại: chấp nhận mọi đăng nhập để test UI
        if (password.length() < 3) {
            errorLabel.setText("Mật khẩu tối thiểu 3 ký tự!");
            return;
        }

        // Lưu thông tin session
        UserSession.getInstance().login(username, role);

        // Chuyển sang màn hình phù hợp theo vai trò
        try {
            String fxml = role.equals("SELLER") ? "SellerDashboard.fxml" : "AuctionList.fxml";
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/bidplaza/ui/" + fxml));
            Scene scene = new Scene(loader.load(), 900, 600);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("BidPlaza - " + role);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            errorLabel.setText("Lỗi chuyển màn hình: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
