package com.bidplaza.ui.controller;

import com.bidplaza.network.AuctionSnapshot;
import com.bidplaza.network.Message;
import com.bidplaza.ui.AppStyles;
import com.bidplaza.ui.net.ServerClient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class HistoryController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private TableView<AuctionSnapshot> historyTable;
    @FXML private TableColumn<AuctionSnapshot, String> colName;
    @FXML private TableColumn<AuctionSnapshot, String> colStarting;
    @FXML private TableColumn<AuctionSnapshot, String> colFinal;
    @FXML private TableColumn<AuctionSnapshot, String> colWinner;
    @FXML private TableColumn<AuctionSnapshot, String> colEndTime;
    @FXML private TableColumn<AuctionSnapshot, String> colStatus;

    private static final DateTimeFormatter DTF =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        javafx.application.Platform.runLater(() -> {
            if (historyTable.getScene() != null) {
                AppStyles.applyTo(historyTable.getScene());
            }
            loadHistory();
        });
    }

    private void setupColumns() {
        colName.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        colStarting.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(
                String.format("$%.2f", c.getValue().getStartingPrice())));
        colFinal.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(
                String.format("$%.2f", c.getValue().getCurrentPrice())));
        colWinner.setCellValueFactory(c -> {
            String winner = c.getValue().getWinnerUsername();
            if (winner == null) {
                winner = c.getValue().getWinnerId() != null
                    ? c.getValue().getWinnerId() : "—";
            }
            return new javafx.beans.property.SimpleStringProperty(winner);
        });
        colEndTime.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(
                c.getValue().getEndTime() != null
                    ? c.getValue().getEndTime().format(DTF) : "—"));
        colStatus.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
    }

    private void loadHistory() {
        try {
            Message response = ServerClient.request(
                new Message(Message.Type.GET_AUCTION_HISTORY, null));
            if (response.isSuccess() && response.getPayload() instanceof List<?> list) {
                List<AuctionSnapshot> snapshots = list.stream()
                    .filter(AuctionSnapshot.class::isInstance)
                    .map(AuctionSnapshot.class::cast)
                    .toList();
                historyTable.setItems(FXCollections.observableArrayList(snapshots));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        loadHistory();
    }

    @FXML
    private void handleExportCSV() {
        try {
            String path = com.bidplaza.util.CsvExporter.buildDefaultPath("AuctionHistory");
            String[] headers = {"Tên sản phẩm", "Giá khởi điểm", "Giá cuối", "Người thắng", "Kết thúc lúc", "Trạng thái"};
            java.util.List<String[]> rows = historyTable.getItems().stream()
                .map(s -> new String[]{
                    s.getName(),
                    String.format("$%.2f", s.getStartingPrice()),
                    String.format("$%.2f", s.getCurrentPrice()),
                    s.getWinnerUsername() != null ? s.getWinnerUsername() : (s.getWinnerId() != null ? s.getWinnerId() : "—"),
                    s.getEndTime() != null ? s.getEndTime().format(DTF) : "—",
                    s.getStatus()
                }).collect(java.util.stream.Collectors.toList());
            com.bidplaza.util.CsvExporter.export(headers, rows, path);
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Export thành công: " + path);
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Lỗi export: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/bidplaza/ui/BidderDashboard.fxml"));
            Scene scene = new Scene(loader.load());
            AppStyles.applyTo(scene);
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("BidPlaza - Bidder Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
