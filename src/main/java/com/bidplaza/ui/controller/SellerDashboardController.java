package com.bidplaza.ui.controller;

import com.bidplaza.model.Notification;
import com.bidplaza.network.AuctionSnapshot;
import com.bidplaza.network.CreateAuctionRequest;
import com.bidplaza.network.Message;
import com.bidplaza.ui.AppStyles;
import com.bidplaza.ui.model.AuctionItem;
import com.bidplaza.ui.model.UserSession;
import com.bidplaza.ui.net.ServerClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class SellerDashboardController implements Initializable {

    @FXML private Label userLabel;
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
    @FXML private Button bellButton;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ObservableList<AuctionItem> items = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userLabel.setText(UserSession.getInstance().getUsername());

        categoryCombo.setItems(FXCollections.observableArrayList(
                "electronics", "art", "vehicle"));
        categoryCombo.setValue("electronics");

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("startPrice"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colBids.setCellValueFactory(new PropertyValueFactory<>("endTime"));

        itemTable.setItems(items);

        // Load danh sách phiên của Seller từ Server
        loadSellerAuctions();
        syncNotifications();

        Platform.runLater(() -> {
            try {
                Scene scene = userLabel.getScene();
                if (scene != null) {
                    scene.getStylesheets().add(
                        getClass().getResource("/com/bidplaza/ui/style.css").toExternalForm()
                    );
                }
            } catch (Exception ignored) {}
        });
    }

    private void loadSellerAuctions() {
        String sellerId = UserSession.getInstance().getUserId();
        String sellerUsername = UserSession.getInstance().getUsername();
        new Thread(() -> {
            try {
                Message response = ServerClient.request(
                        new Message(Message.Type.LIST_AUCTIONS, null));

                if (response.getPayload() instanceof List<?> list) {
                    Platform.runLater(() -> {
                        items.clear();
                        for (Object obj : list) {
                            if (obj instanceof AuctionSnapshot snap) {
                                if (sellerId.equals(snap.getSellerId())
                                        || sellerUsername.equals(snap.getSellerId())) {
                                    items.add(snapshotToItem(snap));
                                }
                            }
                        }
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() ->
                        showResult("Không thể tải danh sách: " + e.getMessage(), false));
            }
        }).start();
    }

    @FXML
    private void handleAddItem() {
        String name     = itemNameField.getText().trim();
        String category = categoryCombo.getValue();
        String desc     = descField != null ? descField.getText().trim() : "";
        String priceStr = startPriceField.getText().trim();
        int duration    = durationSpinner.getValue();

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

        String sellerId = UserSession.getInstance().getUserId();
        CreateAuctionRequest req = new CreateAuctionRequest(
                name, desc, category, price, duration, sellerId);

        new Thread(() -> {
            try {
                Message response = ServerClient.request(
                        new Message(Message.Type.CREATE_AUCTION, req));

                Platform.runLater(() -> {
                    if (response.getPayload() instanceof AuctionSnapshot snap) {
                        items.add(snapshotToItem(snap));
                        showResult("Đã đăng sản phẩm: " + name, true);
                        clearForm();
                    } else {
                        String err = response.getInfo();
                        showResult(err != null ? err : "Lỗi tạo phiên đấu giá!", false);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        showResult("Không thể kết nối Server!", false));
            }
        }).start();
    }

    @FXML
    private void handleFinishAuction() {
        AuctionItem selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showResult("Chưa chọn sản phẩm!", false);
            return;
        }

        new Thread(() -> {
            try {
                Message response = ServerClient.request(
                        new Message(Message.Type.FINISH_AUCTION,
                                selected.getId(), null, 0, null));

                Platform.runLater(() -> {
                    if (response.getType() != Message.Type.ERROR) {
                        selected.setStatus("FINISHED");
                        itemTable.refresh();
                        showResult("Phiên đấu giá đã kết thúc.", true);
                    } else {
                        String err = response.getInfo();
                        showResult(err != null ? err : "Không thể kết thúc phiên!", false);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        showResult("Không thể kết nối Server!", false));
            }
        }).start();
    }

    @FXML
    private void handleRefresh() {
        loadSellerAuctions();
    }

    @FXML
    private void openProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/bidplaza/ui/Profile.fxml"));
            Scene scene = new Scene(loader.load());
            AppStyles.applyTo(scene);
            Stage stage = (Stage) userLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("BidPlaza - Profile");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        UserSession.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/bidplaza/ui/Login.fxml"));
            Scene scene = new Scene(loader.load(), 500, 500);
            AppStyles.applyTo(scene);
            Stage stage = (Stage) userLabel.getScene().getWindow();
            stage.setTitle("BidPlaza - Đăng nhập");
            stage.setScene(scene);
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
                "$" + snap.getStartingPrice(),
                "$" + snap.getCurrentPrice(),
                snap.getStatus(),
                endTime
        );
    }

    private void showResult(String msg, boolean success) {
        formResultLabel.setStyle(success
                ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
        formResultLabel.setText(msg);
    }

    private void clearForm() {
        itemNameField.clear();
        if (descField != null) descField.clear();
        startPriceField.clear();
        categoryCombo.setValue("electronics");
    }

    private void syncNotifications() {
        new Thread(() -> {
            try {
                Message response = ServerClient.request(new Message(
                    Message.Type.GET_NOTIFICATIONS, null,
                    UserSession.getInstance().getUserId(), 0, null));
                if (response.getPayload() instanceof List<?> list) {
                    java.util.List<Notification> notifications = new java.util.ArrayList<>();
                    for (Object object : list) {
                        if (object instanceof Notification notification) {
                            notifications.add(notification);
                        }
                    }
                    UserSession.setNotifications(notifications);
                    Platform.runLater(this::updateBellBadge);
                }
            } catch (Exception ignored) {
                Platform.runLater(this::updateBellBadge);
            }
        }, "seller-notifications-load").start();
    }

    private void updateBellBadge() {
        if (bellButton != null) {
            bellButton.setText("Bell (" + UserSession.getUnreadCount() + ")");
        }
    }

    @FXML
    private void handleOpenNotifications() {
        syncNotifications();

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Thong bao");
        ListView<Notification> listView = new ListView<>();
        listView.setItems(FXCollections.observableArrayList(
            UserSession.getNotifications()));
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Notification notification, boolean empty) {
                super.updateItem(notification, empty);
                if (empty || notification == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText((notification.isRead() ? "" : "* ")
                    + notification.getTitle()
                    + "\n" + notification.getMessage()
                    + "\n" + notification.getTimestamp().format(
                        DateTimeFormatter.ofPattern("dd/MM HH:mm")));
                setStyle(notification.isRead()
                    ? "-fx-text-fill: #666;"
                    : "-fx-font-weight: bold;");
            }
        });
        dialog.getDialogPane().setContent(listView);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();

        UserSession.getNotifications().forEach(notification -> notification.setRead(true));
        updateBellBadge();
        ServerClient.sendAsync(new Message(
            Message.Type.MARK_NOTIFICATIONS_READ, null,
            UserSession.getInstance().getUserId(), 0, null));
    }
}
