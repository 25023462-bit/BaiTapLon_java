package com.bidplaza.ui.controller;

import com.bidplaza.ui.model.AuctionItem;
import com.bidplaza.ui.model.UserSession;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller màn hình đấu giá realtime.
 *
 * Kết nối Socket đến Server để:
 * - Gửi bid khi người dùng đặt giá
 * - Nhận update realtime khi có người khác đặt giá
 */
public class AuctionDetailController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private Label itemNameLabel;
    @FXML private Label categoryLabel;
    @FXML private Label startPriceLabel;
    @FXML private Label endTimeLabel;
    @FXML private Label currentPriceLabel;
    @FXML private Label leaderLabel;
    @FXML private Label bidResultLabel;
    @FXML private Label statusLabel;
    @FXML private TextField bidAmountField;
    @FXML private ListView<String> bidHistoryList;

    private AuctionItem auction;
    private ObservableList<String> bidHistory = FXCollections.observableArrayList();

    // Socket connection
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean connected = false;

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bidHistoryList.setItems(bidHistory);
    }

    /**
     * Được gọi từ AuctionListController để truyền dữ liệu phiên.
     */
    public void setAuction(AuctionItem auction) {
        this.auction = auction;

        // Hiển thị thông tin
        titleLabel.setText(auction.getName());
        itemNameLabel.setText(auction.getName());
        categoryLabel.setText(auction.getCategory());
        startPriceLabel.setText(auction.getStartPrice());
        endTimeLabel.setText(auction.getEndTime());
        currentPriceLabel.setText(auction.getCurrentPrice());

        // Kết nối Socket đến Server
        connectToServer();
    }

    private void connectToServer() {
        // Chạy trên thread riêng để không block UI
        new Thread(() -> {
            try {
                socket = new Socket(SERVER_HOST, SERVER_PORT);
                out = new ObjectOutputStream(socket.getOutputStream());
                in  = new ObjectInputStream(socket.getInputStream());
                connected = true;

                Platform.runLater(() ->
                    statusLabel.setText("✅ Đã kết nối đến server"));

                // Lắng nghe update từ server liên tục
                listenForUpdates();

            } catch (IOException e) {
                Platform.runLater(() ->
                    statusLabel.setText("⚠️ Không kết nối được server — chạy ở chế độ offline"));
            }
        }).start();
    }

    /**
     * Lắng nghe message từ Server trên background thread.
     * Khi có update → dùng Platform.runLater() để cập nhật UI.
     */
    private void listenForUpdates() {
        try {
            while (connected) {
                Object obj = in.readObject();

                // Xử lý message (dùng reflection-free approach)
                String msgStr = obj.toString();
                Platform.runLater(() -> handleServerMessage(msgStr, obj));
            }
        } catch (Exception e) {
            Platform.runLater(() ->
                statusLabel.setText("❌ Mất kết nối với server"));
        }
    }

    private void handleServerMessage(String msgStr, Object obj) {
        // Kiểm tra loại message qua toString()
        if (msgStr.contains("BID_SUCCESS")) {
            bidResultLabel.setStyle("-fx-text-fill: #27ae60;");
            bidResultLabel.setText("✅ Đặt giá thành công!");
            statusLabel.setText("✅ Bid thành công");

        } else if (msgStr.contains("BID_FAILED")) {
            bidResultLabel.setStyle("-fx-text-fill: #e74c3c;");
            bidResultLabel.setText("❌ Đặt giá thất bại: " + extractInfo(msgStr));

        } else if (msgStr.contains("AUCTION_UPDATE")) {
            // Có người khác vừa đặt giá → cập nhật UI
            String newPrice = extractAmount(msgStr);
            String leader   = extractLeader(msgStr);

            currentPriceLabel.setText("$" + newPrice);
            leaderLabel.setText("Người dẫn đầu: " + leader);
            bidHistory.add(0, "💰 " + leader + " đặt $" + newPrice);

            if (auction != null) {
                auction.setCurrentPrice("$" + newPrice);
            }
        }
    }

    @FXML
    private void handlePlaceBid() {
        String amountStr = bidAmountField.getText().trim();

        if (amountStr.isEmpty()) {
            showBidResult("Vui lòng nhập số tiền!", false);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            showBidResult("Số tiền không hợp lệ!", false);
            return;
        }

        String bidderId = UserSession.getInstance().getUserId();
        String auctionId = auction.getId();

        if (connected && out != null) {
            // Gửi bid đến Server qua Socket
            new Thread(() -> {
                try {
                    // Tạo message dạng text đơn giản
                    // (sau này dùng Message class từ backend)
                    String bidMsg = auctionId + " " + bidderId + " " + amount;
                    out.writeObject(bidMsg);
                    out.flush();

                    Platform.runLater(() -> {
                        bidHistory.add(0, "📤 Bạn đặt $" + amount);
                        bidAmountField.clear();
                    });

                } catch (IOException e) {
                    Platform.runLater(() ->
                        showBidResult("Lỗi kết nối: " + e.getMessage(), false));
                }
            }).start();

        } else {
            // Offline mode: cập nhật UI trực tiếp
            currentPriceLabel.setText("$" + amount);
            leaderLabel.setText("Người dẫn đầu: " + UserSession.getInstance().getUsername());
            bidHistory.add(0, "💰 " + UserSession.getInstance().getUsername() + " đặt $" + amount);
            bidAmountField.clear();
            showBidResult("✅ Đặt giá: $" + amount + " (offline mode)", true);
        }
    }

    @FXML
    private void handleBack() {
        closeSocket();
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/bidplaza/ui/AuctionList.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showBidResult(String message, boolean success) {
        bidResultLabel.setStyle(success
            ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
        bidResultLabel.setText(message);
    }

    private void closeSocket() {
        connected = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }

    // Helper methods để parse toString() của Message
    private String extractInfo(String msg) {
        int i = msg.indexOf("info=");
        if (i == -1) return "Không rõ lý do";
        return msg.substring(i + 5, msg.indexOf("}", i));
    }

    private String extractAmount(String msg) {
        int i = msg.indexOf("amount=");
        if (i == -1) return "0";
        String rest = msg.substring(i + 7);
        return rest.split("[,}]")[0];
    }

    private String extractLeader(String msg) {
        int i = msg.indexOf("bidder=");
        if (i == -1) return "Unknown";
        String rest = msg.substring(i + 7);
        return rest.split("[,}]")[0];
    }
}
