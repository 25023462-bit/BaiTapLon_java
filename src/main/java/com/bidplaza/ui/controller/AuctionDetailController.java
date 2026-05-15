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

                // Gửi ngay 1 message LIST_AUCTIONS để lấy thông tin phiên hiện tại
                out.writeObject(new com.bidplaza.network.Message(com.bidplaza.network.Message.Type.LIST_AUCTIONS, null));
                out.flush();

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
                Platform.runLater(() -> handleServerMessage(obj));
            }
        } catch (Exception e) {
            Platform.runLater(() ->
                statusLabel.setText("❌ Mất kết nối với server"));
        }
    }

    private void handleServerMessage(Object obj) {
        if (obj instanceof com.bidplaza.network.Message) {
            com.bidplaza.network.Message msg = (com.bidplaza.network.Message) obj;
            switch (msg.getType()) {
                case BID_SUCCESS:
                    bidResultLabel.setStyle("-fx-text-fill: #27ae60;");
                    bidResultLabel.setText("✅ Đặt giá thành công!");
                    statusLabel.setText("✅ Bid thành công");
                    
                    // Cập nhật giá mới cho chính mình
                    currentPriceLabel.setText("$" + msg.getAmount());
                    leaderLabel.setText("Người dẫn đầu: Bạn");
                    if (auction != null) {
                        auction.setCurrentPrice("$" + msg.getAmount());
                    }
                    break;
                case BID_FAILED:
                    bidResultLabel.setStyle("-fx-text-fill: #e74c3c;");
                    bidResultLabel.setText("❌ Đặt giá thất bại: " + msg.getInfo());
                    break;
                case AUCTION_UPDATE:
                    // Có người khác hoặc mình vừa đặt giá
                    if (msg.getPayload() instanceof com.bidplaza.network.AuctionSnapshot) {
                        com.bidplaza.network.AuctionSnapshot snapshot = (com.bidplaza.network.AuctionSnapshot) msg.getPayload();
                        currentPriceLabel.setText("$" + snapshot.getCurrentPrice());
                        String l = snapshot.getWinnerId() != null ? snapshot.getWinnerId() : "Chưa có";
                        leaderLabel.setText("Người dẫn đầu: " + l);
                        bidHistory.add(0, "💰 " + l + " đặt $" + snapshot.getCurrentPrice());
                        if (auction != null) {
                            auction.setCurrentPrice("$" + snapshot.getCurrentPrice());
                        }
                    } else {
                        currentPriceLabel.setText("$" + msg.getAmount());
                        leaderLabel.setText("Người dẫn đầu: " + msg.getBidderId());
                        bidHistory.add(0, "💰 " + msg.getBidderId() + " đặt $" + msg.getAmount());
                        if (auction != null) {
                            auction.setCurrentPrice("$" + msg.getAmount());
                        }
                    }
                    break;
                case LIST_AUCTIONS:
                    if (msg.getPayload() instanceof java.util.List) {
                        java.util.List<?> snapshots = (java.util.List<?>) msg.getPayload();
                        for (Object sObj : snapshots) {
                            if (sObj instanceof com.bidplaza.network.AuctionSnapshot) {
                                com.bidplaza.network.AuctionSnapshot s = (com.bidplaza.network.AuctionSnapshot) sObj;
                                if (auction != null && s.getId().equals(auction.getId())) {
                                    currentPriceLabel.setText("$" + s.getCurrentPrice());
                                    String ld = s.getWinnerId() != null ? s.getWinnerId() : "Chưa có";
                                    leaderLabel.setText("Người dẫn đầu: " + ld);
                                    auction.setCurrentPrice("$" + s.getCurrentPrice());
                                }
                            }
                        }
                    }
                    break;
                case ERROR:
                    bidResultLabel.setStyle("-fx-text-fill: #e74c3c;");
                    bidResultLabel.setText("❌ Lỗi Server: " + msg.getInfo());
                    break;
                default:
                    break;
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
                    // Sử dụng Message class từ backend
                    com.bidplaza.network.Message bidMsg = com.bidplaza.network.Message.placeBid(auctionId, bidderId, amount);
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
}
