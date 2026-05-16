package com.bidplaza.ui.controller;

import com.bidplaza.ui.model.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import com.bidplaza.network.Message;

<<<<<<< HEAD:src/java/com/bidplaza/ui/controller/LoginController.java
=======
/**
 * Controller cho màn hình Login
 */
>>>>>>> cade1658b480eadddf16e8af7c5761c766d5d9cd:src/main/java/com/bidplaza/ui/controller/LoginController.java
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
<<<<<<< HEAD:src/java/com/bidplaza/ui/controller/LoginController.java
        roleCombo.getItems().addAll("BIDDER", "SELLER", "ADMIN");
=======

        // Danh sách role
        roleCombo.setItems(
                FXCollections.observableArrayList(
                        "BIDDER",
                        "SELLER",
                        "ADMIN"
                )
        );

        // Role mặc định
>>>>>>> cade1658b480eadddf16e8af7c5761c766d5d9cd:src/main/java/com/bidplaza/ui/controller/LoginController.java
        roleCombo.setValue("BIDDER");
    }

    @FXML
    private void handleLogin() {

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleCombo.getValue();

<<<<<<< HEAD:src/java/com/bidplaza/ui/controller/LoginController.java
=======
        // Validate input
>>>>>>> cade1658b480eadddf16e8af7c5761c766d5d9cd:src/main/java/com/bidplaza/ui/controller/LoginController.java
        if (username.isEmpty() || password.isEmpty()) {

            errorLabel.setText("Vui lòng nhập đầy đủ thông tin!");
            return;
        }
        sendLoginRequest(username, password, role, false);
    }

<<<<<<< HEAD:src/java/com/bidplaza/ui/controller/LoginController.java
    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role     = roleCombo.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Vui lòng nhập đầy đủ thông tin!");
=======
        if (password.length() < 3) {

            errorLabel.setText("Mật khẩu tối thiểu 3 ký tự!");
>>>>>>> cade1658b480eadddf16e8af7c5761c766d5d9cd:src/main/java/com/bidplaza/ui/controller/LoginController.java
            return;
        }
        sendLoginRequest(username, password, role, true);
    }

<<<<<<< HEAD:src/java/com/bidplaza/ui/controller/LoginController.java
    private void sendLoginRequest(String username, String password,
                                  String role, boolean isRegister) {
        try {
            java.net.Socket socket = new java.net.Socket("127.0.0.1", 8080);
            java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(
                    socket.getOutputStream());
            java.io.ObjectInputStream in = new java.io.ObjectInputStream(
                    socket.getInputStream());
=======
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
>>>>>>> cade1658b480eadddf16e8af7c5761c766d5d9cd:src/main/java/com/bidplaza/ui/controller/LoginController.java

            out.writeObject(Message.login(username, password, role, isRegister));
            out.flush();

            Message response = (Message) in.readObject();
            socket.close();

            if (response.getAmount() == 1) {
                if (isRegister) {
                    errorLabel.setStyle("-fx-text-fill: green;");
                    errorLabel.setText("Đăng ký thành công!");
                    sendLoginRequest(username, password, role, false);
                } else {
                    UserSession.getInstance().login(username, role);
                    chuyenManHinh(role);
                }
            } else {
                errorLabel.setStyle("-fx-text-fill: #e74c3c;");
                String msg = response.getInfo();
                errorLabel.setText(msg != null ? msg : "Lỗi không xác định");
            }

        } catch (java.net.ConnectException e) {
            errorLabel.setText("Không thể kết nối Server!");
        } catch (Exception e) {
            errorLabel.setText("Lỗi: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
    private Object createLoginMessage(String username, String password,
                                      String role, String action) throws Exception {
        Class<?> msgClass = Class.forName("com.bidplaza.network.Message");
        Class<?> typeClass = Class.forName("com.bidplaza.network.Message$Type");

        // Tìm đúng enum value thay vì dùng index cứng
        Object loginType = null;
        for (Object enumVal : (Object[]) typeClass.getMethod("values").invoke(null)) {
            if (enumVal.toString().equals("LOGIN")) {
                loginType = enumVal;
                break;
            }
        }

        java.lang.reflect.Constructor<?> ctor = msgClass.getDeclaredConstructor(
                typeClass, String.class, String.class, double.class, String.class);
        ctor.setAccessible(true);
        return ctor.newInstance(loginType, null, username, 0.0,
                password + "|" + role + "|" + action);
    }

    private void chuyenManHinh(String role) {
        try {
            String fxml = role.equals("SELLER") ?
                    "SellerDashboard.fxml" : "AuctionList.fxml";
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/bidplaza/ui/" + fxml));
            Scene scene = new Scene(loader.load(), 900, 600);
            Stage stage = (Stage) usernameField.getScene().getWindow();

            stage.setTitle("BidPlaza - " + role);

            // Fullscreen đẹp hơn
            stage.setMaximized(true);

            stage.setScene(scene);

            stage.show();
        } catch (Exception e) {
<<<<<<< HEAD:src/java/com/bidplaza/ui/controller/LoginController.java
            errorLabel.setText("Lỗi chuyển màn hình: " + e.getMessage());
=======

            errorLabel.setText("Lỗi chuyển màn hình!");

            e.printStackTrace();
>>>>>>> cade1658b480eadddf16e8af7c5761c766d5d9cd:src/main/java/com/bidplaza/ui/controller/LoginController.java
        }
    }
}