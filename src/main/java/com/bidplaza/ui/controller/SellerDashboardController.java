package com.bidplaza.ui.controller;

import com.bidplaza.model.Notification;
import com.bidplaza.network.AuctionSnapshot;
import com.bidplaza.network.CreateAuctionRequest;
import com.bidplaza.network.Message;
import com.bidplaza.ui.AppStyles;
import com.bidplaza.ui.model.AuctionItem;
import com.bidplaza.ui.model.UserSession;
import com.bidplaza.ui.net.ServerClient;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class SellerDashboardController implements Initializable {

    @FXML private Label userLabel;
    @FXML private Label totalAuctionsLabel;
    @FXML private Label activeAuctionsLabel;
    @FXML private Label revenueLabel;
    @FXML private VBox createFormPanel;
    @FXML private VBox auctionsTablePanel;
    @FXML private TextField itemNameField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextArea descField;
    @FXML private TextField startPriceField;
    @FXML private Spinner<Integer> durationSpinner;
    @FXML private Label formResultLabel;
    @FXML private TableView<AuctionItem> itemTable;
    @FXML private TableColumn<AuctionItem, String> colName;
    @FXML private TableColumn<AuctionItem, String> colCategory;
    @FXML private TableColumn<AuctionItem, String> colPrice;
    @FXML private TableColumn<AuctionItem, String> colStatus;
    @FXML private TableColumn<AuctionItem, String> colBids;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ObservableList<AuctionItem> items = FXCollections.observableArrayList();
    private final List<AuctionSnapshot> sellerSnapshots = new ArrayList<>();

    // Auto-refresh timeline: cập nhật bảng mỗi 5 giây để Seller thấy giá mới nhất từ Bidder
    private Timeline autoRefreshTimeline;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String username = UserSession.getInstance().getUsername();
        userLabel.setText(username != null ? username : "SELLER");

        categoryCombo.setItems(FXCollections.observableArrayList(
                "electronics", "art", "vehicle"));
        categoryCombo.setValue("electronics");

        if (durationSpinner.getValueFactory() == null) {
            durationSpinner.setValueFactory(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 168, 24));
        }

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice")); // Hiện giá hiện tại
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colBids.setCellValueFactory(new PropertyValueFactory<>("endTime"));

        // Style text cho table cell để dễ nhìn
        colName.setCellFactory(col -> new TableCell<AuctionItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            }
        });
        colCategory.setCellFactory(col -> new TableCell<AuctionItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-text-fill: #8ea6ff;");
            }
        });
        colPrice.setCellFactory(col -> new TableCell<AuctionItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-text-fill: #00ff99; -fx-font-weight: bold;");
            }
        });
        colStatus.setCellFactory(col -> new TableCell<AuctionItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                if (item != null && (item.contains("RUNNING") || item.contains("OPEN"))) {
                    setStyle("-fx-text-fill: #00ff99; -fx-font-weight: bold;");
                } else if (item != null && item.contains("FINISHED")) {
                    setStyle("-fx-text-fill: #ff4dff;");
                } else {
                    setStyle("-fx-text-fill: #aaaaaa;");
                }
            }
        });
        colBids.setCellFactory(col -> new TableCell<AuctionItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle("-fx-text-fill: rgba(255,255,255,0.75);");
            }
        });

        itemTable.setItems(items);

        // Load lần đầu trên background thread (tránh UI freeze)
        loadSellerAuctions();

        // Tự động refresh mỗi 5 giây để Seller thấy bid mới nhất của Bidder
        startAutoRefresh();
    }

    private void startAutoRefresh() {
        autoRefreshTimeline = new Timeline(
            new KeyFrame(Duration.seconds(5), e -> loadSellerAuctions())
        );
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    private String resolveSellerId() {
        UserSession session = UserSession.getInstance();
        if (session.getUserId() != null && !session.getUserId().isBlank()) {
            return session.getUserId();
        }
        return session.getUsername();
    }

    private boolean isMyAuction(AuctionSnapshot snap) {
        String sellerKey = resolveSellerId();
        if (sellerKey == null || snap.getSellerId() == null) {
            return false;
        }
        String snapSeller = snap.getSellerId();
        if (sellerKey.equals(snapSeller)) {
            return true;
        }
        String username = UserSession.getInstance().getUsername();
        return username != null && username.equals(snapSeller);
    }

    private void loadSellerAuctions() {
        // Chạy trên background thread để không đóng băng UI
        new Thread(() -> {
            try {
                Message response = ServerClient.request(
                        new Message(Message.Type.LIST_AUCTIONS, null));

                if (response.getPayload() instanceof List<?> list) {
                    List<AuctionSnapshot> mine = new ArrayList<>();
                    List<AuctionItem> rows = new ArrayList<>();
                    for (Object obj : list) {
                        if (obj instanceof AuctionSnapshot snap && isMyAuction(snap)) {
                            mine.add(snap);
                            rows.add(snapshotToItem(snap));
                        }
                    }
                    Platform.runLater(() -> {
                        sellerSnapshots.clear();
                        sellerSnapshots.addAll(mine);
                        items.setAll(rows);
                        updateStats();
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() ->
                        showResult("Không thể tải danh sách: " + e.getMessage(), false));
            }
        }, "seller-auctions-load").start();
    }

    private void updateStats() {
        int total = sellerSnapshots.size();
        int active = 0;
        double revenue = 0;

        for (AuctionSnapshot snap : sellerSnapshots) {
            String status = snap.getStatus() != null ? snap.getStatus().toUpperCase() : "";
            if ("RUNNING".equals(status) || "OPEN".equals(status)) {
                active++;
            }
            if ("FINISHED".equals(status) || "PAID".equals(status)) {
                revenue += snap.getCurrentPrice();
            }
        }

        totalAuctionsLabel.setText(String.valueOf(total));
        activeAuctionsLabel.setText(String.valueOf(active));
        revenueLabel.setText(String.format("%,.0f USD", revenue));
    }


    @FXML
    private void handleAddItem() {
        String name = itemNameField.getText().trim();
        String category = categoryCombo.getValue();
        String desc = descField != null ? descField.getText().trim() : "";
        String priceStr = startPriceField.getText().trim().replace(",", ".");

        Integer duration = durationSpinner.getValue();
        if (duration == null) {
            duration = 24;
        }

        if (name.isEmpty() || priceStr.isEmpty()) {
            showResult("Vui lòng nhập đầy đủ thông tin!", false);
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            showResult("Giá khởi điểm không hợp lệ!", false);
            return;
        }

        String sellerId = resolveSellerId();
        CreateAuctionRequest req = new CreateAuctionRequest(
                name, desc, category, price, duration, sellerId);

        showResult("Đang gửi lên server...", true);
        new Thread(() -> {
            try {
                Message response = ServerClient.request(
                        new Message(Message.Type.CREATE_AUCTION, req));

                Platform.runLater(() -> {
                    if (response.getPayload() instanceof AuctionSnapshot snap) {
                        showResult("Đã đăng sản phẩm: " + name, true);
                        clearForm();
                        loadSellerAuctions();
                    } else {
                        String err = response.getInfo();
                        showResult(err != null ? err : "Lỗi tạo phiên đấu giá!", false);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        showResult("Không thể kết nối Server!", false));
            }
        }, "seller-create-auction").start();
    }

    @FXML
    private void handleFinishAuction() {
        AuctionItem selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showResult("Chưa chọn sản phẩm!", false);
            return;
        }

        showResult("Đang kết thúc phiên...", true);
        new Thread(() -> {
            try {
                Message response = ServerClient.request(
                        new Message(Message.Type.FINISH_AUCTION,
                                selected.getId(), null, 0, null));

                Platform.runLater(() -> {
                    if (response.getType() != Message.Type.ERROR) {
                        showResult("Phiên đấu giá đã kết thúc.", true);
                        loadSellerAuctions();
                    } else {
                        String err = response.getInfo();
                        showResult(err != null ? err : "Không thể kết thúc phiên!", false);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        showResult("Không thể kết nối Server!", false));
            }
        }, "seller-finish-auction").start();
    }

    @FXML
    private void handleRefresh() {
        loadSellerAuctions();
    }

    @FXML
    private void handleLogout() {
        // Dừng auto-refresh trước khi rời khỏi màn hình
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }
        UserSession.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/bidplaza/ui/Login.fxml"));
            Scene scene = new Scene(loader.load(), 500, 500);
            AppStyles.applyTo(scene);

            Stage stage = (Stage) userLabel.getScene().getWindow();
            stage.setTitle("BidPlaza - Đăng nhập");
            if (stage.getScene() != null) {
                javafx.scene.Parent rootNode = scene.getRoot();
                scene.setRoot(new javafx.scene.layout.Pane());
                stage.getScene().setRoot(rootNode);
            } else {
                stage.setScene(scene);
            }
            stage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private AuctionItem snapshotToItem(AuctionSnapshot snap) {
        String endTime = snap.getEndTime() != null
                ? snap.getEndTime().format(FMT) : "N/A";

        return new AuctionItem(
                snap.getId(),
                snap.getName(),
                snap.getCategory(),
                String.format("$%.2f", snap.getStartingPrice()),
                String.format("$%.2f", snap.getCurrentPrice()), // giá hiện tại (live)
                snap.getStatus(),
                endTime
        );
    }

    private void showResult(String msg, boolean success) {
        if (formResultLabel != null) {
            formResultLabel.setStyle(success
                    ? "-fx-text-fill: #00ff99; -fx-font-weight: bold;"
                    : "-fx-text-fill: #ff4d4d; -fx-font-weight: bold;");
            formResultLabel.setText(msg);
        }
    }

    private void clearForm() {
        itemNameField.clear();
        if (descField != null) {
            descField.clear();
        }
        startPriceField.clear();
        categoryCombo.setValue("electronics");
    }
}
