package com.bidplaza.ui.controller;

import com.bidplaza.network.AuctionSnapshot;
import com.bidplaza.network.Message;
import com.bidplaza.ui.AppStyles;
import com.bidplaza.ui.model.AuctionItem;
import com.bidplaza.ui.model.UserSession;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller màn hình đấu giá realtime.
 *
 * Phase 3 – tính năng mới:
 *  1. Auto-bid panel: người dùng nhập maxBid + increment → gửi REGISTER_AUTO_BID lên server.
 *  2. LineChart: vẽ đường giá realtime sau mỗi lần có AUCTION_UPDATE.
 *  3. Countdown timer: đếm ngược thời gian còn lại của phiên.
 */
public class AuctionDetailController implements Initializable {

    // ── Info labels ───────────────────────────────────────────────
    @FXML private Label titleLabel;
    @FXML private Label itemNameLabel;
    @FXML private Label categoryLabel;
    @FXML private Label startPriceLabel;
    @FXML private Label endTimeLabel;
    @FXML private Label currentPriceLabel;
    @FXML private Label leaderLabel;
    @FXML private Label bidResultLabel;
    @FXML private Label statusLabel;
    @FXML private Label countdownLabel;   // Phase 3: đếm ngược

    // ── Manual bid ───────────────────────────────────────────────
    @FXML private TextField bidAmountField;
    @FXML private Button placeBidButton;

    // ── Auto-bid panel ───────────────────────────────────────────
    @FXML private TextField maxBidField;
    @FXML private TextField incrementField;
    @FXML private Button autoBidButton;
    @FXML private Label autoBidStatusLabel;

    // ── History + Chart ──────────────────────────────────────────
    @FXML private ListView<String> bidHistoryList;
    @FXML private LineChart<Number, Number> priceChart;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;

    // ── State ────────────────────────────────────────────────────
    private AuctionItem auction;
    private final ObservableList<String> bidHistory = FXCollections.observableArrayList();

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean connected = false;

    private final AtomicInteger bidTick = new AtomicInteger(0);
    private XYChart.Series<Number, Number> priceSeries;

    private static final String SERVER_HOST = "localhost";
    private static final int    SERVER_PORT = 8080;

    // ── Initialize ───────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bidHistoryList.setItems(bidHistory);
        setupChart();
    }

    private void setupChart() {
        if (priceChart == null) return; // không có chart trong FXML cũ → skip
        priceSeries = new XYChart.Series<>();
        priceSeries.setName("Giá đấu");
        priceChart.getData().add(priceSeries);
        priceChart.setAnimated(false);
        priceChart.setCreateSymbols(true);
        if (xAxis != null) xAxis.setLabel("Lần bid");
        if (yAxis != null) yAxis.setLabel("Giá ($)");
    }

    // ── Public API ───────────────────────────────────────────────

    public void setAuction(AuctionItem auction) {
        this.auction = auction;

        titleLabel.setText(auction.getName());
        itemNameLabel.setText(auction.getName());
        categoryLabel.setText(auction.getCategory());
        startPriceLabel.setText(auction.getStartPrice());
        endTimeLabel.setText(auction.getEndTime());
        currentPriceLabel.setText(auction.getCurrentPrice());

        // Điểm dữ liệu đầu tiên trên chart = giá khởi điểm
        addChartPoint(parsePrice(auction.getCurrentPrice()));

        connectToServer();
        startCountdown(auction.getEndTime());
    }

    // ── Networking ───────────────────────────────────────────────

    private void connectToServer() {
        new Thread(() -> {
            try {
                socket = new Socket(SERVER_HOST, SERVER_PORT);
                out = new ObjectOutputStream(socket.getOutputStream());
                in  = new ObjectInputStream(socket.getInputStream());
                connected = true;

                Platform.runLater(() -> statusLabel.setText("Đã kết nối đến server"));

                out.writeObject(new Message(Message.Type.LIST_AUCTIONS, null));
                out.flush();

                listenForUpdates();
            } catch (IOException e) {
                Platform.runLater(() ->
                    statusLabel.setText("Không kết nối được server – offline mode"));
            }
        }, "socket-listener").start();
    }

    private void listenForUpdates() {
        try {
            while (connected) {
                Object obj = in.readObject();
                Platform.runLater(() -> handleServerMessage(obj));
            }
        } catch (Exception e) {
            Platform.runLater(() -> statusLabel.setText("Mất kết nối với server"));
        }
    }

    private void handleServerMessage(Object obj) {
        if (!(obj instanceof Message msg)) return;

        switch (msg.getType()) {

            case BID_SUCCESS -> {
                showBidResult("Đặt giá thành công!", true);
                updatePrice(msg.getAmount(), UserSession.getInstance().getUsername());
            }

            case BID_FAILED -> showBidResult("Thất bại: " + msg.getInfo(), false);

            case AUCTION_UPDATE -> {
                if (msg.getPayload() instanceof AuctionSnapshot snap) {
                    updatePrice(snap.getCurrentPrice(),
                        snap.getWinnerId() != null ? snap.getWinnerId() : "?");
                } else {
                    updatePrice(msg.getAmount(), msg.getBidderId());
                }
            }

            case AUTO_BID_SUCCESS -> {
                if (autoBidStatusLabel != null)
                    autoBidStatusLabel.setStyle("-fx-text-fill: #27ae60;");
                if (autoBidStatusLabel != null)
                    autoBidStatusLabel.setText("Auto-bid kích hoạt đến $" + msg.getAmount());
                if (autoBidButton != null)
                    autoBidButton.setDisable(true);
            }

            case AUTO_BID_FAILED -> {
                if (autoBidStatusLabel != null)
                    autoBidStatusLabel.setStyle("-fx-text-fill: #e74c3c;");
                if (autoBidStatusLabel != null)
                    autoBidStatusLabel.setText("Lỗi: " + msg.getInfo());
            }

            case LIST_AUCTIONS -> {
                if (msg.getPayload() instanceof java.util.List<?> list) {
                    for (Object o : list) {
                        if (o instanceof AuctionSnapshot s
                                && auction != null && s.getId().equals(auction.getId())) {
                            updatePrice(s.getCurrentPrice(),
                                s.getWinnerId() != null ? s.getWinnerId() : "Chưa có");
                        }
                    }
                }
            }

            case ERROR -> showBidResult("Lỗi Server: " + msg.getInfo(), false);

            default -> { /* ignore */ }
        }
    }

    // ── Manual Bid ───────────────────────────────────────────────

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

        String bidderId  = UserSession.getInstance().getUserId();
        String auctionId = auction.getId();

        if (connected && out != null) {
            new Thread(() -> {
                try {
                    out.writeObject(Message.placeBid(auctionId, bidderId, amount));
                    out.flush();
                    Platform.runLater(() -> {
                        bidHistory.add(0, "Bạn đặt $" + amount);
                        bidAmountField.clear();
                    });
                } catch (IOException e) {
                    Platform.runLater(() ->
                        showBidResult("Lỗi kết nối: " + e.getMessage(), false));
                }
            }, "bid-thread").start();
        } else {
            // Offline mode
            updatePrice(amount, UserSession.getInstance().getUsername());
            bidAmountField.clear();
            showBidResult("Đặt giá: $" + amount + " (offline)", true);
        }
    }

    // ── Auto-bid ─────────────────────────────────────────────────

    /**
     * Gửi yêu cầu REGISTER_AUTO_BID lên server.
     * Server sẽ gọi auction.registerAutoBid() → auto-bid kích hoạt ngay khi có bid mới.
     */
    @FXML
    private void handleRegisterAutoBid() {
        if (maxBidField == null || incrementField == null) return;

        String maxStr = maxBidField.getText().trim();
        String incStr = incrementField.getText().trim();

        if (maxStr.isEmpty() || incStr.isEmpty()) {
            if (autoBidStatusLabel != null)
                autoBidStatusLabel.setText("Nhập đầy đủ MaxBid và Bước tăng!");
            return;
        }

        double maxBid, increment;
        try {
            maxBid    = Double.parseDouble(maxStr);
            increment = Double.parseDouble(incStr);
        } catch (NumberFormatException e) {
            if (autoBidStatusLabel != null)
                autoBidStatusLabel.setText("Số tiền không hợp lệ!");
            return;
        }

        double currentPrice = parsePrice(currentPriceLabel.getText());
        if (maxBid <= currentPrice) {
            if (autoBidStatusLabel != null)
                autoBidStatusLabel.setText("MaxBid phải lớn hơn giá hiện tại $" + currentPrice);
            return;
        }

        String bidderId  = UserSession.getInstance().getUserId();
        String auctionId = auction.getId();

        if (connected && out != null) {
            new Thread(() -> {
                try {
                    out.writeObject(Message.registerAutoBid(
                        auctionId, bidderId, maxBid, increment));
                    out.flush();
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        if (autoBidStatusLabel != null)
                            autoBidStatusLabel.setText("Lỗi gửi: " + e.getMessage());
                    });
                }
            }, "autobid-register").start();
        } else {
            if (autoBidStatusLabel != null)
                autoBidStatusLabel.setText("Chưa kết nối server – không thể đăng ký auto-bid");
        }
    }

    // ── Helpers ──────────────────────────────────────────────────

    /**
     * Cập nhật giá hiện tại trên UI và thêm điểm lên chart.
     */
    private void updatePrice(double price, String leaderId) {
        currentPriceLabel.setText("$" + price);
        leaderLabel.setText("Người dẫn đầu: " + (leaderId != null ? leaderId : "?"));
        bidHistory.add(0, (leaderId != null ? leaderId : "?") + " đặt $" + price);
        if (auction != null) auction.setCurrentPrice("$" + price);
        addChartPoint(price);
    }

    private void addChartPoint(double price) {
        if (priceSeries == null) return;
        int tick = bidTick.incrementAndGet();
        priceSeries.getData().add(new XYChart.Data<>(tick, price));
        // Giữ tối đa 50 điểm để chart không quá dày
        if (priceSeries.getData().size() > 50) {
            priceSeries.getData().remove(0);
        }
    }

    private void showBidResult(String message, boolean success) {
        bidResultLabel.setStyle(success
            ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
        bidResultLabel.setText(message);
    }

    private double parsePrice(String priceStr) {
        if (priceStr == null) return 0;
        try {
            return Double.parseDouble(priceStr.replace("$", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Đếm ngược đến endTime bằng JavaFX Timeline (chạy trên UI thread).
     */
    private void startCountdown(String endTimeStr) {
        if (countdownLabel == null || endTimeStr == null || endTimeStr.isBlank()) return;
        try {
            java.time.LocalDateTime endTime =
                java.time.LocalDateTime.parse(endTimeStr.trim());

            javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                    javafx.util.Duration.seconds(1),
                    e -> {
                        long secondsLeft = java.time.LocalDateTime.now()
                            .until(endTime, java.time.temporal.ChronoUnit.SECONDS);
                        if (secondsLeft <= 0) {
                            countdownLabel.setText("⏰ Phiên đã kết thúc");
                            countdownLabel.setStyle("-fx-text-fill: #e74c3c;");
                        } else {
                            long hours   = secondsLeft / 3600;
                            long minutes = (secondsLeft % 3600) / 60;
                            long secs    = secondsLeft % 60;
                            countdownLabel.setText(
                                String.format("⏱ %02d:%02d:%02d", hours, minutes, secs));
                            countdownLabel.setStyle(secondsLeft <= 30
                                ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;"
                                : "-fx-text-fill: #27ae60;");
                        }
                    }
                )
            );
            timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
            timeline.play();
        } catch (Exception ignored) {}
    }

    // ── Navigation ───────────────────────────────────────────────

    @FXML
    private void handleBack() {
        closeSocket();
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/bidplaza/ui/AuctionList.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            AppStyles.applyTo(scene);
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeSocket() {
        connected = false;
        try { if (socket != null) socket.close(); }
        catch (IOException ignored) {}
    }
}
