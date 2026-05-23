package com.bidplaza.ui.controller;

import com.bidplaza.network.AuctionSnapshot;
import com.bidplaza.network.Message;
import com.bidplaza.ui.AppStyles;
import com.bidplaza.ui.model.UserSession;
import com.bidplaza.ui.net.ServerClient;
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
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller màn hình Admin.
 *
 * Chức năng:
 * - Xem danh sách tất cả phiên đấu giá + trạng thái
 * - Kết thúc phiên bất kỳ (FINISH_AUCTION)
 * - Xem thống kê: tổng phiên, đang chạy, đã kết thúc
 */
public class AdminDashboardController implements Initializable {

    @FXML private Label userLabel;
    @FXML private Label totalLabel;
    @FXML private Label runningLabel;
    @FXML private Label finishedLabel;
    @FXML private Label statusLabel;

    @FXML private TableView<AuctionRow> auctionTable;
    @FXML private TableColumn<AuctionRow, String> colId;
    @FXML private TableColumn<AuctionRow, String> colName;
    @FXML private TableColumn<AuctionRow, String> colStatus;
    @FXML private TableColumn<AuctionRow, String> colPrice;
    @FXML private TableColumn<AuctionRow, String> colWinner;
    @FXML private TableColumn<AuctionRow, String> colBids;

    private final ObservableList<AuctionRow> rows = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userLabel.setText("Admin: " + UserSession.getInstance().getUsername());

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colWinner.setCellValueFactory(new PropertyValueFactory<>("winner"));
        colBids.setCellValueFactory(new PropertyValueFactory<>("bids"));

        auctionTable.setItems(rows);
        loadAuctions();

        javafx.application.Platform.runLater(() -> {
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

    @FXML
    private void handleRefresh() {
        loadAuctions();
    }

    @FXML
    private void handleExportCSV() {
        try {
            String path = com.bidplaza.util.CsvExporter.buildDefaultPath("AdminAuctions");
            String[] headers = {"ID", "Tên sản phẩm", "Trạng thái", "Giá hiện tại", "Người dẫn đầu", "Số bid"};
            java.util.List<String[]> rows = auctionTable.getItems().stream()
                .map(r -> new String[]{
                    r.getId(),
                    r.getName(),
                    r.getStatus(),
                    r.getPrice(),
                    r.getWinner(),
                    r.getBids()
                }).collect(java.util.stream.Collectors.toList());
            com.bidplaza.util.CsvExporter.export(headers, rows, path);
            showStatus("Đã export CSV: " + path, true);
        } catch (Exception e) {
            showStatus("Lỗi export CSV: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFinishSelected() {
        AuctionRow selected = auctionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Vui lòng chọn 1 phiên để kết thúc!", false);
            return;
        }
        if (!"RUNNING".equals(selected.getStatus())) {
            showStatus("Chỉ có thể kết thúc phiên đang RUNNING!", false);
            return;
        }
        try {
            Message req = new Message(Message.Type.FINISH_AUCTION,
                selected.getId(), null, 0, null);
            ServerClient.request(req);
            showStatus("Đã kết thúc phiên: " + selected.getName(), true);
            loadAuctions();
        } catch (Exception e) {
            showStatus("Lỗi: " + e.getMessage(), false);
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
            stage.setScene(scene);
            stage.setTitle("BidPlaza - Login");
        } catch (Exception e) {
            showStatus("Lỗi đăng xuất: " + e.getMessage(), false);
        }
    }

    private void loadAuctions() {
        try {
            Message req = new Message(Message.Type.LIST_AUCTIONS, null);
            Message res = ServerClient.request(req);

            if (res.getPayload() instanceof List<?> list) {
                rows.clear();
                for (Object o : list) {
                    if (o instanceof AuctionSnapshot s) {
                        rows.add(new AuctionRow(
                            s.getId().substring(0, 8) + "...",
                            s.getId(),
                            s.getName(),
                            s.getStatus(),
                            "$" + s.getCurrentPrice(),
                            s.getWinnerId() != null ? s.getWinnerId() : "—",
                            String.valueOf(s.getBidCount())
                        ));
                    }
                }
                updateStats();
                showStatus("Đã tải " + rows.size() + " phiên.", true);
            }
        } catch (Exception e) {
            showStatus("Không kết nối được server: " + e.getMessage(), false);
        }
    }

    private void updateStats() {
        totalLabel.setText(String.valueOf(rows.size()));
        long running  = rows.stream().filter(r -> "RUNNING".equals(r.getStatus())).count();
        long finished = rows.stream().filter(r -> "FINISHED".equals(r.getStatus())
                                              || "PAID".equals(r.getStatus())).count();
        runningLabel.setText(String.valueOf(running));
        finishedLabel.setText(String.valueOf(finished));
    }

    private void showStatus(String msg, boolean ok) {
        statusLabel.setStyle(ok ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
        statusLabel.setText(msg);
    }

    // ── Inner model class cho TableView ──────────────────────────

    public static class AuctionRow {
        private final String shortId;
        private final String id;
        private final String name;
        private final String status;
        private final String price;
        private final String winner;
        private final String bids;

        public AuctionRow(String shortId, String id, String name, String status,
                          String price, String winner, String bids) {
            this.shortId = shortId;
            this.id      = id;
            this.name    = name;
            this.status  = status;
            this.price   = price;
            this.winner  = winner;
            this.bids    = bids;
        }

        public String getId()      { return id; }
        public String getShortId() { return shortId; }
        public String getName()    { return name; }
        public String getStatus()  { return status; }
        public String getPrice()   { return price; }
        public String getWinner()  { return winner; }
        public String getBids()    { return bids; }
    }
}
