package com.bidplaza.ui.controller;

import com.bidplaza.ui.AppStyles;
import com.bidplaza.ui.model.AuctionItem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javafx.scene.control.cell.PropertyValueFactory;

import javafx.stage.Stage;

public class MyBidsController {

    @FXML
    private TableView<AuctionItem> bidsTable;

    @FXML
    private TableColumn<AuctionItem, String> nameColumn;

    @FXML
    private TableColumn<AuctionItem, String> categoryColumn;

    @FXML
    private TableColumn<AuctionItem, String> currentBidColumn;

    @FXML
    private TableColumn<AuctionItem, String> statusColumn;

    @FXML
    private TableColumn<AuctionItem, String> endTimeColumn;

    @FXML
    public void initialize() {

        // MAP COLUMN

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        categoryColumn.setCellValueFactory(
                new PropertyValueFactory<>("category")
        );

        currentBidColumn.setCellValueFactory(
                new PropertyValueFactory<>("currentPrice")
        );

        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        endTimeColumn.setCellValueFactory(
                new PropertyValueFactory<>("endTime")
        );

        // SAMPLE DATA

        ObservableList<AuctionItem> data =
                FXCollections.observableArrayList(

                        new AuctionItem(
                                "1",
                                "Cyber Car",
                                "Vehicle",
                                "5000",
                                "12450 USD",
                                "WINNING",
                                "00:05:32"
                        ),

                        new AuctionItem(
                                "2",
                                "Neon Bike",
                                "Vehicle",
                                "3000",
                                "8200 USD",
                                "OUTBID",
                                "00:12:45"
                        )
                );

        bidsTable.setItems(data);
    }

    @FXML
    private void goBack() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/bidplaza/ui/BidderDashboard.fxml"
                    )
            );

            Scene scene = new Scene(loader.load());

            AppStyles.applyTo(scene);

            Stage stage =
                    (Stage) bidsTable.getScene().getWindow();

            stage.setScene(scene);

            stage.setTitle("BidPlaza - Bidder Dashboard");

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}