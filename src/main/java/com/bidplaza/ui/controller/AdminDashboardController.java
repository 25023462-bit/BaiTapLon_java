package com.bidplaza.ui.controller;

import com.bidplaza.network.AuctionSnapshot;
import com.bidplaza.network.Message;
import com.bidplaza.network.SystemStats;
import com.bidplaza.network.UserInfo;
import com.bidplaza.ui.AppStyles;
import com.bidplaza.ui.model.UserSession;
import com.bidplaza.ui.net.ServerClient;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private Label userLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label totalBiddersLabel;
    @FXML private Label totalSellersLabel;
    @FXML private Label runningLabel;
    @FXML private Label revenueLabel;
    @FXML private Label statusLabel;
    @FXML private TableView<UserInfo> usersTable;
    @FXML private TableView<AuctionSnapshot> auctionsTable;

    private final ObservableList<UserInfo> users = FXCollections.observableArrayList();
    private final ObservableList<AuctionSnapshot> auctions = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (userLabel != null) {
            userLabel.setText("Admin: " + UserSession.getInstance().getUsername());
        }
        setupTables();
        loadStats();
        loadUsers();
        loadRunningAuctions();
    }

    private void setupTables() {
        usersTable.setItems(users);
        usersTable.getColumns().clear();
        usersTable.getColumns().add(textColumn("Username", UserInfo::getUsername, 150));
        usersTable.getColumns().add(textColumn("Role", UserInfo::getRole, 90));
        usersTable.getColumns().add(textColumn("Email", UserInfo::getEmail, 190));
        usersTable.getColumns().add(textColumn("Balance",
            u -> "$" + String.format("%.2f", u.getBalance()), 95));
        usersTable.getColumns().add(textColumn("Banned",
            u -> u.isBanned() ? "Yes" : "No", 80));
        usersTable.getColumns().add(userActionColumn());

        auctionsTable.setItems(auctions);
        auctionsTable.getColumns().clear();
        auctionsTable.getColumns().add(auctionColumn("Name", AuctionSnapshot::getName, 190));
        auctionsTable.getColumns().add(auctionColumn("Current Price",
            a -> "$" + String.format("%.2f", a.getCurrentPrice()), 110));
        auctionsTable.getColumns().add(auctionColumn("Bids",
            a -> String.valueOf(a.getBidCount()), 70));
        auctionsTable.getColumns().add(auctionColumn("End Time",
            a -> a.getEndTime() != null ? a.getEndTime().format(DATE_FORMAT) : "", 145));
        auctionsTable.getColumns().add(auctionActionColumn());
    }

    private TableColumn<UserInfo, String> textColumn(
            String title, java.util.function.Function<UserInfo, String> mapper,
            double width) {
        TableColumn<UserInfo, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(data ->
            new ReadOnlyStringWrapper(mapper.apply(data.getValue())));
        return column;
    }

    private TableColumn<AuctionSnapshot, String> auctionColumn(
            String title, java.util.function.Function<AuctionSnapshot, String> mapper,
            double width) {
        TableColumn<AuctionSnapshot, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(data ->
            new ReadOnlyStringWrapper(mapper.apply(data.getValue())));
        return column;
    }

    private TableColumn<UserInfo, UserInfo> userActionColumn() {
        TableColumn<UserInfo, UserInfo> column = new TableColumn<>("Actions");
        column.setPrefWidth(100);
        column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        column.setCellFactory(col -> new TableCell<>() {
            private final Button button = new Button("⚠ BAN");

            {
                button.setStyle("""
                    -fx-background-color: transparent;
                    -fx-border-color: #ff00ff;
                    -fx-border-radius: 8;
                    -fx-background-radius: 8;
                    -fx-text-fill: #ff00ff;
                    -fx-font-weight: bold;
                """);
            }
            @Override
            protected void updateItem(UserInfo user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                    return;
                }
                button.setDisable(user.isBanned());
                button.setOnAction(e -> handleBanUser(user));
                setGraphic(button);
            }
        });
        return column;
    }

    private TableColumn<AuctionSnapshot, AuctionSnapshot> auctionActionColumn() {
        TableColumn<AuctionSnapshot, AuctionSnapshot> column = new TableColumn<>("Actions");
        column.setPrefWidth(120);
        column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
        column.setCellFactory(col -> new TableCell<>() {
            private final Button button = new Button("⚡ CLOSE");
            {
                button.setStyle("""
                    -fx-background-color: transparent;
                    -fx-border-color: #ff4d6d;
                    -fx-border-radius: 8;
                    -fx-background-radius: 8;
                    -fx-text-fill: #ff4d6d;
                    -fx-font-weight: bold;
                """);
            }
            @Override
            protected void updateItem(AuctionSnapshot auction, boolean empty) {
                super.updateItem(auction, empty);
                if (empty || auction == null) {
                    setGraphic(null);
                    return;
                }
                button.setOnAction(e -> handleForceClose(auction));
                setGraphic(button);
            }
        });
        return column;
    }

    private void loadStats() {
        try {
            Message response = ServerClient.request(
                new Message(Message.Type.GET_SYSTEM_STATS, null));
            if (response.getPayload() instanceof SystemStats stats) {
                totalUsersLabel.setText(String.valueOf(stats.getTotalUsers()));
                totalBiddersLabel.setText(String.valueOf(stats.getTotalBidders()));
                totalSellersLabel.setText(String.valueOf(stats.getTotalSellers()));
                runningLabel.setText(String.valueOf(stats.getRunningAuctions()));
                revenueLabel.setText("$" + String.format("%.2f",
                    stats.getTotalTransactionValue()));
            }
        } catch (Exception e) {
            showStatus("Khong tai duoc thong ke: " + e.getMessage(), false);
        }
    }

    private void loadUsers() {
        try {
            Message response = ServerClient.request(
                new Message(Message.Type.GET_ALL_USERS, null));
            users.clear();
            if (response.getPayload() instanceof List<?> list) {
                for (Object object : list) {
                    if (object instanceof UserInfo user) {
                        users.add(user);
                    }
                }
            }
        } catch (Exception e) {
            showStatus("Khong tai duoc users: " + e.getMessage(), false);
        }
    }

    private void loadRunningAuctions() {
        try {
            Message response = ServerClient.request(
                new Message(Message.Type.LIST_AUCTIONS, null));
            auctions.clear();
            if (response.getPayload() instanceof List<?> list) {
                for (Object object : list) {
                    if (object instanceof AuctionSnapshot auction
                            && "RUNNING".equals(auction.getStatus())) {
                        auctions.add(auction);
                    }
                }
            }
            showStatus("Dashboard da cap nhat.", true);
        } catch (Exception e) {
            showStatus("Khong tai duoc phien dau gia: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleRefresh() {
        loadStats();
        loadUsers();
        loadRunningAuctions();
    }

    private void handleBanUser(UserInfo userInfo) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Khoa tai khoan " + userInfo.getUsername() + "?");
        confirm.showAndWait().ifPresent(button -> {
            if (button == ButtonType.OK) {
                try {
                    ServerClient.request(new Message(
                        Message.Type.BAN_USER, null, userInfo.getId(), 0, null));
                    loadUsers();
                    loadStats();
                    showStatus("Da khoa user " + userInfo.getUsername(), true);
                } catch (Exception e) {
                    showStatus("Khong khoa duoc user: " + e.getMessage(), false);
                }
            }
        });
    }

    private void handleForceClose(AuctionSnapshot auction) {
        try {
            ServerClient.request(new Message(
                Message.Type.ADMIN_FORCE_CLOSE, auction.getId(), null, 0, null));
            loadStats();
            loadRunningAuctions();
            showStatus("Da dong phien " + auction.getName(), true);
        } catch (Exception e) {
            showStatus("Khong dong duoc phien: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handleLogout() {
        UserSession.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/bidplaza/ui/Login.fxml"));
            Scene scene = new Scene(loader.load());
            AppStyles.applyTo(scene);
            Stage stage = (Stage) userLabel.getScene().getWindow();
            if (stage.getScene() != null) {
                javafx.scene.Parent rootNode = scene.getRoot();
                scene.setRoot(new javafx.scene.layout.Pane());
                stage.getScene().setRoot(rootNode);
            } else {
                stage.setScene(scene);
            }
            stage.setMaximized(true);
            
            stage.setTitle("BidPlaza - Login");
        } catch (Exception e) {
            showStatus("Loi dang xuat: " + e.getMessage(), false);
        }
    }

    private void showStatus(String message, boolean success) {
        if (statusLabel != null) {
            statusLabel.setStyle(success
                ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
            statusLabel.setText(message);
        }
    }
}
