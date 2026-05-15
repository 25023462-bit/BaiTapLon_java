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

    @FXML
    private Label userLabel;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<AuctionItem> auctionTable;

    @FXML
    private TableColumn<AuctionItem, String> colName;

    @FXML
    private TableColumn<AuctionItem, String> colCategory;

    @FXML
    private TableColumn<AuctionItem, String> colStartPrice;

    @FXML
    private TableColumn<AuctionItem, String> colCurrentPrice;

    @FXML
    private TableColumn<AuctionItem, String> colStatus;

    @FXML
    private TableColumn<AuctionItem, String> colEndTime;

    private ObservableList<AuctionItem> auctionData =
            FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Hiển thị user
        UserSession session = UserSession.getInstance();

        userLabel.setText(
                "👤 " + session.getUsername()
                        + " (" + session.getRole() + ")"
        );

        // Mapping column
        colName.setCellValueFactory(
                new PropertyValueFactory<>("name"));

        colCategory.setCellValueFactory(
                new PropertyValueFactory<>("category"));

        colStartPrice.setCellValueFactory(
                new PropertyValueFactory<>("startPrice"));

        colCurrentPrice.setCellValueFactory(
                new PropertyValueFactory<>("currentPrice"));

        colStatus.setCellValueFactory(
                new PropertyValueFactory<>("status"));

        colEndTime.setCellValueFactory(
                new PropertyValueFactory<>("endTime"));

        // Load data
        loadSampleData();

        auctionTable.setItems(auctionData);
    }

    private void loadSampleData() {

        try {

            com.bidplaza.network.Message request =
                    new com.bidplaza.network.Message(
                            com.bidplaza.network.Message.Type.GET_AUCTION_LIST,
                            null
                    );

            com.bidplaza.network.Message response =
                    com.bidplaza.ui.net.ServerClient.request(request);

            if (response.getType()
                    == com.bidplaza.network.Message.Type.LIST_AUCTIONS) {

                java.util.List<?> snapshots =
                        (java.util.List<?>) response.getPayload();

                for (Object sObj : snapshots) {

                    if (sObj instanceof com.bidplaza.network.AuctionSnapshot) {

                        com.bidplaza.network.AuctionSnapshot s =
                                (com.bidplaza.network.AuctionSnapshot) sObj;

                        java.time.format.DateTimeFormatter dtf =
                                java.time.format.DateTimeFormatter.ofPattern(
                                        "yyyy-MM-dd HH:mm"
                                );

                        auctionData.add(
                                new AuctionItem(
                                        s.getId(),
                                        s.getName(),
                                        s.getCategory(),
                                        "$" + s.getStartingPrice(),
                                        "$" + s.getCurrentPrice(),
                                        s.getStatus(),
                                        s.getEndTime().format(dtf)
                                )
                        );
                    }
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    "Lỗi kết nối",
                    "Không thể tải danh sách phiên đấu giá từ Server!"
            );
        }
    }

    @FXML
    private void handleRefresh() {

        auctionData.clear();

        loadSampleData();
    }

    @FXML
    private void handleJoinAuction() {

        AuctionItem selected =
                auctionTable.getSelectionModel().getSelectedItem();

        if (selected == null) {

            showAlert(
                    "Chưa chọn phiên",
                    "Vui lòng chọn 1 phiên đấu giá trước!"
            );

            return;
        }

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/bidplaza/ui/AuctionDetail.fxml"
                    )
            );

            Scene scene = new Scene(loader.load());

            // Truyền dữ liệu
            AuctionDetailController controller =
                    loader.getController();

            controller.setAuction(selected);

            Stage stage =
                    (Stage) auctionTable.getScene().getWindow();

            stage.setTitle(
                    "BidPlaza - " + selected.getName()
            );

            stage.setMaximized(true);

            stage.setScene(scene);

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();

            showAlert(
                    "Lỗi tham gia phiên",
                    "Không mở được màn hình chi tiết!"
            );
        }
    }

    @FXML
    private void handleLogout() {

        UserSession.getInstance().logout();

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/bidplaza/ui/Login.fxml"
                    )
            );

            Scene scene = new Scene(loader.load());

            Stage stage =
                    (Stage) userLabel.getScene().getWindow();

            stage.setTitle("BidPlaza - Login");

            // Full màn hình
            stage.setMaximized(true);

            stage.setScene(scene);

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {

        Alert alert =
                new Alert(Alert.AlertType.WARNING);

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }
}