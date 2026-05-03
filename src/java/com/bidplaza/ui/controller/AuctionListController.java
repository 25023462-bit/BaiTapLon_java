package com.bidplaza.ui.controller;

import com.bidplaza.ui.model.AuctionItem;
import com.bidplaza.ui.model.UserSession;
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
import java.util.ResourceBundle;

public class AuctionListController implements Initializable {

    @FXML private Label userLabel;
    @FXML private TextField searchField;
    @FXML private TableView<AuctionItem> auctionTable;
    @FXML private TableColumn<AuctionItem, String> colName;
    @FXML private TableColumn<AuctionItem, String> colCategory;
    @FXML private TableColumn<AuctionItem, String> colStartPrice;
    @FXML private TableColumn<AuctionItem, String> colCurrentPrice;
    @FXML private TableColumn<AuctionItem, String> colStatus;
    @FXML private TableColumn<AuctionItem, String> colEndTime;

    private ObservableList<AuctionItem> auctionData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Hiện tên user
        UserSession session = UserSession.getInstance();
        userLabel.setText("👤 " + session.getUsername() + " (" + session.getRole() + ")");

        // Gán cột với property của AuctionItem
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colStartPrice.setCellValueFactory(new PropertyValueFactory<>("startPrice"));
        colCurrentPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));

        // Load dữ liệu mẫu (sau này lấy từ Server qua Socket)
        loadSampleData();
        auctionTable.setItems(auctionData);
    }

    private void loadSampleData() {
        // TODO: thay bằng gọi Socket đến Server
        auctionData.add(new AuctionItem(
            "sample-id-1", "iPhone 15 Pro", "ELECTRONICS",
            "$1,000", "$1,500", "RUNNING", "2026-05-01 20:00"
        ));
        auctionData.add(new AuctionItem(
            "sample-id-2", "Toyota Camry 2024", "VEHICLE",
            "$10,000", "$12,500", "RUNNING", "2026-05-02 18:00"
        ));
        auctionData.add(new AuctionItem(
            "sample-id-3", "Tranh sơn dầu", "ART",
            "$500", "$500", "OPEN", "2026-05-03 15:00"
        ));
    }

    @FXML
    private void handleRefresh() {
        // TODO: gọi Server lấy danh sách mới nhất
        auctionData.clear();
        loadSampleData();
    }

    @FXML
    private void handleJoinAuction() {
        AuctionItem selected = auctionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn phiên", "Vui lòng chọn 1 phiên đấu giá trước!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/bidplaza/ui/AuctionDetail.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);

            // Truyền dữ liệu phiên sang màn hình chi tiết
            AuctionDetailController controller = loader.getController();
            controller.setAuction(selected);

            Stage stage = (Stage) auctionTable.getScene().getWindow();
            stage.setTitle("BidPlaza - " + selected.getName());
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi tham gia phiên", "Không mở được màn hình chi tiết: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        UserSession.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/bidplaza/ui/Login.fxml"));
            Scene scene = new Scene(loader.load(), 500, 500);
            Stage stage = (Stage) userLabel.getScene().getWindow();
            stage.setTitle("BidPlaza - Đăng nhập");
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
