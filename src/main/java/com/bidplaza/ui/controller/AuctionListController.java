package com.bidplaza.ui.controller;

import com.bidplaza.ui.AppStyles;
import com.bidplaza.ui.model.AuctionItem;
import com.bidplaza.ui.model.UserSession;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private FilteredList<AuctionItem> filteredData;

    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<String> statusFilter;

    private Timeline timeline;
    private final java.util.Map<String, com.bidplaza.network.AuctionSnapshot> snapshotById =
            new java.util.HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // User info
        UserSession session = UserSession.getInstance();

        userLabel.setText(
                session.getUsername()
                        + " (" + session.getRole() + ")"
        );

        // Mapping columns
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

        // Load auctions
        loadSampleData();

        // Setup filters
        categoryFilter.setItems(FXCollections.observableArrayList(
                "All", "electronics", "art", "vehicle"));
        categoryFilter.setValue("All");

        statusFilter.setItems(FXCollections.observableArrayList(
                "All", "LIVE", "ENDED"));
        statusFilter.setValue("All");

        filteredData = new FilteredList<>(
                auctionData,
                p -> true
        );
        SortedList<AuctionItem> sortedData =
                new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(auctionTable.comparatorProperty());
        auctionTable.setItems(sortedData);

        searchField.textProperty().addListener((obs, old, val) -> applyFilter());
        categoryFilter.valueProperty().addListener((obs, old, val) -> applyFilter());
        statusFilter.valueProperty().addListener((obs, old, val) -> applyFilter());

        // Start realtime countdown
        startCountdown();

        javafx.application.Platform.runLater(() -> {
            try {

                Scene scene = userLabel.getScene();

                if (scene != null) {

                    scene.getStylesheets().add(
                            getClass()
                                    .getResource("/com/bidplaza/ui/style.css")
                                    .toExternalForm()
                    );
                }

            } catch (Exception ignored) {
            }
        });
    }

    private void loadSampleData() {
        snapshotById.clear();

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

                        DateTimeFormatter dtf =
                                DateTimeFormatter.ofPattern(
                                        "yyyy-MM-dd HH:mm"
                                );

                        snapshotById.put(s.getId(), s);
                        auctionData.add(
                                new AuctionItem(
                                        s.getId(),
                                        s.getName(),
                                        s.getCategory(),
                                        "$" + s.getStartingPrice(),
                                        "$" + s.getCurrentPrice(),
                                        "LIVE",
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
    private void handleBack() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/bidplaza/ui/BidderDashboard.fxml"
                    )
            );

            Scene scene = new Scene(loader.load());

            AppStyles.applyTo(scene);

            Stage stage =
                    (Stage) auctionTable.getScene().getWindow();

            stage.setTitle("BidPlaza - BIDDER");

            stage.setMaximized(true);

            stage.setScene(scene);

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
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

            AppStyles.applyTo(scene);

            AuctionDetailController controller =
                    loader.getController();

            controller.setAuction(selected, snapshotById.get(selected.getId()));

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

            AppStyles.applyTo(scene);

            Stage stage =
                    (Stage) userLabel.getScene().getWindow();

            stage.setTitle("BidPlaza - Login");

            stage.setMaximized(true);

            stage.setScene(scene);

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void startCountdown() {

        timeline = new Timeline(

                new KeyFrame(Duration.seconds(1), e -> {

                    for (AuctionItem item : auctionData) {

                        try {

                            LocalDateTime endTime =
                                    LocalDateTime.parse(
                                            item.getEndTime(),
                                            DateTimeFormatter.ofPattern(
                                                    "yyyy-MM-dd HH:mm"
                                            )
                                    );

                            java.time.Duration duration =
                                    java.time.Duration.between(
                                            LocalDateTime.now(),
                                            endTime
                                    );

                            long seconds =
                                    duration.getSeconds();

                            if (seconds <= 0) {

                                item.setStatus("ENDED");

                            } else {

                                long h = seconds / 3600;
                                long m = (seconds % 3600) / 60;
                                long s = seconds % 60;

                                item.setStatus(
                                        String.format(
                                                "LIVE %02d:%02d:%02d",
                                                h, m, s
                                        )
                                );
                            }

                        } catch (Exception ignored) {
                        }
                    }

                    auctionTable.refresh();
                })
        );

        timeline.setCycleCount(
                Timeline.INDEFINITE
        );

        timeline.play();
    }

    private void showAlert(String title, String message) {

        Alert alert =
                new Alert(Alert.AlertType.WARNING);

        alert.setTitle(title);

        alert.setHeaderText(null);

        alert.setContentText(message);

        alert.showAndWait();
    }

    private void applyFilter() {
        filteredData.setPredicate(item -> {
            String search = searchField.getText() == null
                    ? ""
                    : searchField.getText().toLowerCase().trim();
            String category = categoryFilter.getValue();
            String status = statusFilter.getValue();

            boolean matchSearch = search.isEmpty()
                    || item.getName().toLowerCase().contains(search)
                    || item.getCategory().toLowerCase().contains(search);
            boolean matchCategory = "All".equals(category) || category == null
                    || item.getCategory().equalsIgnoreCase(category);
            boolean matchStatus = "All".equals(status) || status == null
                    || item.getStatus().contains(status);

            return matchSearch && matchCategory && matchStatus;
        });
    }

    @FXML
    private void handleClearFilter() {
        searchField.clear();
        categoryFilter.setValue("All");
        statusFilter.setValue("All");
    }
}
