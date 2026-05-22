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

import javafx.scene.layout.BorderPane;

import javafx.stage.Stage;

public class HistoryController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private TableView<AuctionItem> historyTable;

    @FXML
    private TableColumn<AuctionItem, String> nameColumn;

    @FXML
    private TableColumn<AuctionItem, String> categoryColumn;

    @FXML
    private TableColumn<AuctionItem, String> bidColumn;

    @FXML
    private TableColumn<AuctionItem, String> statusColumn;

    @FXML
    private TableColumn<AuctionItem, String> timeColumn;

    @FXML
    public void initialize() {

        // MAP COLUMN

        nameColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

        categoryColumn.setCellValueFactory(
                new PropertyValueFactory<>("category")
        );

        bidColumn.setCellValueFactory(
                new PropertyValueFactory<>("currentPrice")
        );

        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        timeColumn.setCellValueFactory(
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
                                "WON",
                                "2026-05-20 21:15"
                        ),

                        new AuctionItem(
                                "2",
                                "Neon Helmet",
                                "Accessory",
                                "1200",
                                "3500 USD",
                                "LOST",
                                "2026-05-19 18:40"
                        )
                );

        historyTable.setItems(data);

        javafx.application.Platform.runLater(() -> {
            try {
                Scene scene = historyTable.getScene();
                if (scene != null) {
                    scene.getStylesheets().add(
                        getClass().getResource("/com/bidplaza/ui/style.css").toExternalForm()
                    );
                }
            } catch (Exception ignored) {}
        });
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
                    (Stage) rootPane.getScene().getWindow();

            stage.setScene(scene);

            stage.setTitle("BidPlaza - Dashboard");

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}