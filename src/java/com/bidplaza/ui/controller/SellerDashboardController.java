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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private ObservableList<AuctionItem> items = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userLabel.setText("👤 " + UserSession.getInstance().getUsername());

        categoryCombo.setItems(FXCollections.observableArrayList(
            "electronics", "art", "vehicle"));
        categoryCombo.setValue("electronics");

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("startPrice"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colBids.setCellValueFactory(new PropertyValueFactory<>("endTime"));

        itemTable.setItems(items);
    }

    @FXML
    private void handleAddItem() {
        String name      = itemNameField.getText().trim();
        String category  = categoryCombo.getValue();
        String priceStr  = startPriceField.getText().trim();
        int duration     = durationSpinner.getValue();

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

        // Tính thời gian kết thúc
        String endTime = LocalDateTime.now()
            .plusHours(duration)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        // Tạo AuctionItem mới
        AuctionItem newItem = new AuctionItem(
            "item-" + System.currentTimeMillis(),
            name, category,
            "$" + price, "$" + price,
            "OPEN", endTime
        );
        items.add(newItem);

        // TODO: gửi lên Server qua Socket

        showResult("✅ Đã đăng sản phẩm: " + name, true);
        clearForm();
    }

    @FXML
    private void handleFinishAuction() {
        AuctionItem selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showResult("Chưa chọn sản phẩm!", false);
            return;
        }
        selected.setStatus("FINISHED");
        itemTable.refresh();
        showResult("Phiên đấu giá đã kết thúc.", true);
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

    private void showResult(String msg, boolean success) {
        formResultLabel.setStyle(success
            ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
        formResultLabel.setText(msg);
    }

    private void clearForm() {
        itemNameField.clear();
        descField.clear();
        startPriceField.clear();
        categoryCombo.setValue("electronics");
    }
}
