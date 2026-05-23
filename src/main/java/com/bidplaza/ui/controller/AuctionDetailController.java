package com.bidplaza.ui.controller;

import com.bidplaza.network.AuctionSnapshot;
import com.bidplaza.network.BidTransactionInfo;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

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
    private javafx.animation.Timeline countdownTimeline;

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

    // Chat room
    @FXML private ListView<String> chatListView;
    @FXML private TextField chatInput;

    // Review panel
    @FXML private VBox ratingSection;
    @FXML private Spinner<Integer> ratingSpinner;
    @FXML private TextArea reviewCommentField;
    @FXML private Label reviewResultLabel;

    // ── State ────────────────────────────────────────────────────
    private AuctionItem auction;
    private AuctionSnapshot currentSnapshot;
    private final ObservableList<String> bidHistory = FXCollections.observableArrayList();

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean connected = false;

    private int chartTick = 0;
    private XYChart.Series<Number, Number> priceSeries;

    private static final Map<String, List<double[]>> chartCache = new ConcurrentHashMap<>();

    private static final String SERVER_HOST = "localhost";
    private static final int    SERVER_PORT = 8080;

    // ── Initialize ───────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bidHistoryList.setItems(bidHistory);
        setupChart();

        Platform.runLater(() -> {
            try {
                Scene scene = bidHistoryList.getScene();
                if (scene != null) {
                    scene.getStylesheets().add(
                        getClass().getResource("/com/bidplaza/ui/style.css").toExternalForm()
                    );
                }
            } catch (Exception ignored) {}
        });
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
        setAuction(auction, null);
    }

    public void setAuction(AuctionItem auction, AuctionSnapshot snapshot) {
        this.auction = auction;
        this.currentSnapshot = snapshot;

        titleLabel.setText(auction.getName());
        itemNameLabel.setText(auction.getName());
        categoryLabel.setText(auction.getCategory());
        startPriceLabel.setText(auction.getStartPrice());
        endTimeLabel.setText(auction.getEndTime());
        currentPriceLabel.setText(auction.getCurrentPrice());
        updateRatingVisibility();

        loadExistingBidHistory();

        connectToServer();
        startCountdown(auction.getEndTime());
    }

    private AuctionSnapshot getCurrentSnapshot() {
        return currentSnapshot;
    }

    private void loadExistingBidHistory() {
        if (priceSeries != null) {
            priceSeries.getData().clear();
        }
        bidHistory.clear();
        chartTick = 0;

        String auctionId = auction != null ? auction.getId() : null;
        if (auctionId != null && chartCache.containsKey(auctionId)) {
            for (double[] point : chartCache.get(auctionId)) {
                bidHistory.add(String.format("Giá $%.2f", point[1]));
                if (priceSeries != null) {
                    priceSeries.getData().add(new XYChart.Data<>(point[0], point[1]));
                }
                chartTick = (int) point[0] + 1;
            }
            return;
        }

        AuctionSnapshot snapshot = getCurrentSnapshot();
        if (snapshot == null) {
            if (auction != null) {
                addChartPoint(parsePrice(auction.getCurrentPrice()));
            }
            return;
        }

        List<BidTransactionInfo> history = snapshot.getBidHistory();
        if (history == null || history.isEmpty()) {
            addChartPoint(parsePrice(auction.getCurrentPrice()));
            return;
        }

        for (BidTransactionInfo tx : history) {
            bidHistory.add(tx.getAuctionName() + " đặt $" + tx.getAmount());
            if (priceSeries != null) {
                priceSeries.getData().add(new XYChart.Data<>(chartTick, tx.getAmount()));
            }
            chartTick++;
        }
        saveChartCache();
    }

    private void saveChartCache() {
        if (auction == null || priceSeries == null) {
            return;
        }
        List<double[]> points = new ArrayList<>();
        for (XYChart.Data<Number, Number> d : priceSeries.getData()) {
            points.add(new double[] { d.getXValue().doubleValue(), d.getYValue().doubleValue() });
        }
        chartCache.put(auction.getId(), points);
    }

    // ── Networking ───────────────────────────────────────────────

    private void connectToServer() {
        new Thread(() -> {
            try {
                socket = new Socket(SERVER_HOST, com.bidplaza.network.ServerPort.get());
                out = new ObjectOutputStream(socket.getOutputStream());
                in  = new ObjectInputStream(socket.getInputStream());
                connected = true;

                Platform.runLater(() -> statusLabel.setText("Đã kết nối đến server"));

                sendToServer(new Message(
                    Message.Type.JOIN_AUCTION,
                    auction.getId(),
                    UserSession.getInstance().getUserId(),
                    0,
                    null
                ));
                sendToServer(new Message(Message.Type.GET_AUCTION_LIST, null));

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
                    currentSnapshot = snap;
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

            case CHAT_MESSAGE -> {
                if (chatListView != null && msg.getPayload() instanceof com.bidplaza.network.ChatMessage chat) {
                    chatListView.getItems().add(
                        chat.getSenderUsername() + ": " + chat.getContent()
                    );
                }
            }

            case REVIEW_RESPONSE -> {
                if (reviewResultLabel != null) {
                    reviewResultLabel.setText(msg.getInfo());
                    reviewResultLabel.setStyle(msg.isSuccess()
                        ? "-fx-text-fill: #27ae60;"
                        : "-fx-text-fill: #e74c3c;");
                }
            }

            case LIST_AUCTIONS -> {
                if (msg.getPayload() instanceof java.util.List<?> list) {
                    for (Object o : list) {
                        if (o instanceof AuctionSnapshot s
                                && auction != null && s.getId().equals(auction.getId())) {
                            currentSnapshot = s;
                            updatePrice(s.getCurrentPrice(),
                                s.getWinnerId() != null ? s.getWinnerId() : "Chưa có");
                        }
                    }
                }
            }

            case ERROR -> showBidResult("Lỗi Server: " + msg.getInfo(), false);

            case OUTBID -> Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Bi vuot gia!");
                alert.setHeaderText("Ban vua bi vuot gia!");
                alert.setContentText(msg.getInfo());
                alert.show();
            });
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
                    sendToServer(Message.placeBid(auctionId, bidderId, amount));
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
                    sendToServer(Message.registerAutoBid(
                        auctionId, bidderId, maxBid, increment));
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
    @FXML
    private void handleChatKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleSendChat();
            event.consume();
        }
    }

    @FXML
    private void handleSendChat() {
        if (chatInput == null || auction == null) return;

        String content = chatInput.getText().trim();
        if (content.isEmpty()) return;

        if (!connected || out == null) {
            showBidResult("Chua ket noi server nen khong gui duoc chat.", false);
            return;
        }

        com.bidplaza.network.ChatMessage chat =
            new com.bidplaza.network.ChatMessage(
                auction.getId(),
                UserSession.getInstance().getUserId(),
                UserSession.getInstance().getUsername(),
                content
            );

        new Thread(() -> {
            try {
                sendToServer(new Message(Message.Type.CHAT_MESSAGE, chat));
                Platform.runLater(chatInput::clear);
            } catch (IOException e) {
                Platform.runLater(() ->
                    showBidResult("Loi gui chat: " + e.getMessage(), false));
            }
        }, "chat-send").start();
    }

    @FXML
    private void handleSubmitReview() {
        if (auction == null || ratingSpinner == null) return;

        AuctionSnapshot snapshot = getCurrentSnapshot();
        if (snapshot == null || snapshot.getSellerId() == null) {
            if (reviewResultLabel != null) {
                reviewResultLabel.setText("Khong tim thay thong tin nguoi ban.");
                reviewResultLabel.setStyle("-fx-text-fill: #e74c3c;");
            }
            return;
        }

        if (!connected || out == null) {
            if (reviewResultLabel != null) {
                reviewResultLabel.setText("Chua ket noi server.");
                reviewResultLabel.setStyle("-fx-text-fill: #e74c3c;");
            }
            return;
        }

        String comment = reviewCommentField != null
            ? reviewCommentField.getText().trim()
            : "";

        com.bidplaza.network.ReviewRequest request =
            new com.bidplaza.network.ReviewRequest(
                UserSession.getInstance().getUserId(),
                snapshot.getSellerId(),
                auction.getId(),
                ratingSpinner.getValue(),
                comment
            );

        new Thread(() -> {
            try {
                sendToServer(new Message(Message.Type.SUBMIT_REVIEW, request));
            } catch (IOException e) {
                Platform.runLater(() -> {
                    if (reviewResultLabel != null) {
                        reviewResultLabel.setText("Loi gui danh gia: " + e.getMessage());
                        reviewResultLabel.setStyle("-fx-text-fill: #e74c3c;");
                    }
                });
            }
        }, "review-submit").start();
    }

    private void updatePrice(double price, String leaderId) {
        currentPriceLabel.setText("$" + price);
        leaderLabel.setText("Người dẫn đầu: " + (leaderId != null ? leaderId : "?"));
        bidHistory.add(0, (leaderId != null ? leaderId : "?") + " đặt $" + price);
        if (auction != null) auction.setCurrentPrice("$" + price);
        addChartPoint(price);
        updateRatingVisibility();
    }

    private void addChartPoint(double price) {
        if (priceSeries == null) return;
        priceSeries.getData().add(new XYChart.Data<>(chartTick++, price));
        if (priceSeries.getData().size() > 50) {
            priceSeries.getData().remove(0);
        }
        saveChartCache();
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
    private void updateRatingVisibility() {
        if (ratingSection == null || currentSnapshot == null) return;

        String currentUserId = UserSession.getInstance().getUserId();
        boolean canReview =
            "FINISHED".equals(currentSnapshot.getStatus())
                && currentUserId != null
                && currentUserId.equals(currentSnapshot.getWinnerId());

        ratingSection.setVisible(canReview);
    }

    private synchronized void sendToServer(Message message) throws IOException {
        if (out == null) {
            throw new IOException("Chua co ket noi server");
        }
        out.writeObject(message);
        out.flush();
    }

    private void startCountdown(String endTimeStr) {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        if (countdownLabel == null || endTimeStr == null || endTimeStr.isBlank()) return;
        try {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            java.time.LocalDateTime endTime =
                java.time.LocalDateTime.parse(endTimeStr.trim(), formatter);

            countdownTimeline = new javafx.animation.Timeline(
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
            countdownTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
            countdownTimeline.play();
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
        try {
            if (out != null && auction != null) {
                sendToServer(new Message(
                    Message.Type.LEAVE_AUCTION,
                    auction.getId(),
                    UserSession.getInstance().getUserId(),
                    0,
                    null
                ));
            }
        } catch (IOException ignored) {}
        if (countdownTimeline != null) {
            countdownTimeline.stop();
            countdownTimeline = null;
        }
        try { if (socket != null) socket.close(); }
        catch (IOException ignored) {}
    }
}
