package com.bidplaza.ui.controller;

import com.bidplaza.network.BidHistoryResponse;
import com.bidplaza.network.BidTransactionInfo;
import com.bidplaza.network.Message;
import com.bidplaza.ui.AppStyles;
import com.bidplaza.ui.model.UserSession;
import com.bidplaza.ui.net.ServerClient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class MyBidsController implements Initializable {

    @FXML private TableView<BidTransactionInfo> bidsTable;
    @FXML private TableColumn<BidTransactionInfo, String> colAuction;
    @FXML private TableColumn<BidTransactionInfo, Double> colAmount;
    @FXML private TableColumn<BidTransactionInfo, String> colTime;
    @FXML private TableColumn<BidTransactionInfo, String> colStatus;

    private static final DateTimeFormatter DTF =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        javafx.application.Platform.runLater(() -> {
            if (bidsTable.getScene() != null) {
                AppStyles.applyTo(bidsTable.getScene());
            }
            loadBidHistory();
        });
    }

    private void setupColumns() {
        colAuction.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(c.getValue().getAuctionName()));
        colAmount.setCellValueFactory(c ->
            new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getAmount()));
        colAmount.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("$%.2f", item));
            }
        });
        colTime.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(
                c.getValue().getTimestamp().format(DTF)));
        colStatus.setCellValueFactory(c ->
            new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                switch (item) {
                    case "WON" -> setTextFill(Color.web("#27ae60"));
                    case "LOST" -> setTextFill(Color.web("#e74c3c"));
                    default -> setTextFill(Color.web("#3498db"));
                }
            }
        });
    }

    private void loadBidHistory() {
        try {
            String bidderId = UserSession.getInstance().getUserId();
            Message response = ServerClient.request(
                new Message(Message.Type.GET_MY_BIDS, null, bidderId, 0, null));
            if (response.isSuccess() && response.getPayload() instanceof BidHistoryResponse data) {
                bidsTable.setItems(FXCollections.observableArrayList(data.getBids()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRefresh() {
        loadBidHistory();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/bidplaza/ui/BidderDashboard.fxml"));
            Scene scene = new Scene(loader.load());
            AppStyles.applyTo(scene);
            Stage stage = (Stage) bidsTable.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("BidPlaza - Bidder Dashboard");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
