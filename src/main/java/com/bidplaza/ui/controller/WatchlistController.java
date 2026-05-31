package com.bidplaza.ui.controller;

import com.bidplaza.network.AuctionSnapshot;
import com.bidplaza.network.Message;
import com.bidplaza.ui.AppStyles;
import com.bidplaza.ui.model.AuctionItem;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.geometry.Pos;

import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import javafx.stage.Stage;

import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.List;

public class WatchlistController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private VBox watchlistContainer;

    @FXML
    private TextField searchField;

    private final List<AuctionSnapshot> allAuctions =
            new ArrayList<>();

    @FXML
    public void initialize() {

        javafx.application.Platform.runLater(() -> {

            try {

                Scene scene = rootPane.getScene();

                if (scene != null) {

                    scene.getStylesheets().add(
                            getClass()
                                    .getResource("/com/bidplaza/ui/style.css")
                                    .toExternalForm()
                    );
                }

            } catch (Exception ignored) {}
        });

        loadWatchlist();

        searchField.textProperty().addListener(
                (obs, oldValue, newValue) ->
                        filterAuctions(newValue)
        );
    }

    private void loadWatchlist() {

        try {

            watchlistContainer.getChildren().clear();

            Message request =
                    new Message(
                            Message.Type.GET_AUCTION_LIST,
                            null
                    );

            Message response =
                    com.bidplaza.ui.net.ServerClient
                            .request(request);

            if (response.getType()
                    == Message.Type.LIST_AUCTIONS) {

                List<?> snapshots =
                        (List<?>) response.getPayload();

                allAuctions.clear();

                for (Object obj : snapshots) {

                    if (obj instanceof AuctionSnapshot snapshot) {

                        allAuctions.add(snapshot);

                        watchlistContainer
                                .getChildren()
                                .add(
                                        createAuctionCard(snapshot)
                                );
                    }
                }
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void filterAuctions(String keyword) {

        watchlistContainer.getChildren().clear();

        for (AuctionSnapshot snapshot : allAuctions) {

            if (snapshot.getName()
                    .toLowerCase()
                    .contains(keyword.toLowerCase())) {

                watchlistContainer.getChildren().add(
                        createAuctionCard(snapshot)
                );
            }
        }
    }

    private HBox createAuctionCard(
            AuctionSnapshot snapshot
    ) {

        HBox card = new HBox();

        card.setAlignment(Pos.CENTER_LEFT);
        card.setSpacing(25);
        card.setPrefHeight(170);

        card.setStyle("""
            -fx-background-color: rgba(15,20,45,0.92);
            -fx-background-radius: 20;
            -fx-border-color: #00e5ff;
            -fx-border-radius: 20;
            -fx-border-width: 2;
            -fx-padding: 20;
        """);

        // ================= LEFT =================

        VBox leftBox = new VBox();
        leftBox.setSpacing(12);

        Label nameLabel =
                new Label(snapshot.getName());

        nameLabel.setStyle("""
            -fx-text-fill: white;
            -fx-font-size: 26;
            -fx-font-weight: bold;
        """);

        Label bidLabel =
                new Label(
                        "Current Bid : $"
                                + snapshot.getCurrentPrice()
                );

        bidLabel.setStyle("""
            -fx-text-fill: #00e5ff;
            -fx-font-size: 18;
            -fx-font-weight: bold;
        """);

        Label categoryLabel =
                new Label(
                        "Category : "
                                + snapshot.getCategory()
                );

        categoryLabel.setStyle("""
            -fx-text-fill: white;
            -fx-font-size: 16;
        """);

        leftBox.getChildren().addAll(
                nameLabel,
                bidLabel,
                categoryLabel
        );

        // ================= SPACER =================

        Region spacer = new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        // ================= RIGHT =================

        VBox rightBox = new VBox();

        rightBox.setSpacing(15);

        rightBox.setAlignment(
                Pos.CENTER_RIGHT
        );

        Label statusLabel =
                new Label(snapshot.getStatus());

        if ("RUNNING".equalsIgnoreCase(snapshot.getStatus())) {

            statusLabel.setStyle("""
                -fx-background-color: #00e676;
                -fx-text-fill: black;
                -fx-font-weight: bold;
                -fx-padding: 5 15 5 15;
                -fx-background-radius: 20;
            """);

        } else {

            statusLabel.setStyle("""
                -fx-background-color: #ff5252;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-padding: 5 15 5 15;
                -fx-background-radius: 20;
            """);
        }

        Label endLabel =
                new Label();

        endLabel.setStyle("""
            -fx-text-fill: #ff4dff;
            -fx-font-size: 15;
            -fx-font-weight: bold;
        """);

        updateCountdown(
                endLabel,
                snapshot.getEndTime()
        );

        Timeline timeline =
                new Timeline(
                        new KeyFrame(
                                Duration.seconds(1),
                                e -> updateCountdown(
                                        endLabel,
                                        snapshot.getEndTime()
                                )
                        )
                );

        timeline.setCycleCount(
                Timeline.INDEFINITE
        );

        timeline.play();

        HBox buttonBox = new HBox();

        buttonBox.setSpacing(10);

        Button viewButton =
                new Button("VIEW");

        viewButton.setStyle("""
            -fx-background-color: rgba(0,229,255,0.15);
            -fx-border-color: #00e5ff;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-cursor: hand;
        """);

        viewButton.setOnAction(
                e -> openAuction(snapshot)
        );

        Button removeButton =
                new Button("REMOVE");

        removeButton.setStyle("""
            -fx-background-color: rgba(255,0,255,0.15);
            -fx-border-color: #ff00ff;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-background-radius: 12;
            -fx-border-radius: 12;
            -fx-cursor: hand;
        """);

        removeButton.setOnAction(e -> {

            watchlistContainer
                    .getChildren()
                    .remove(card);

            allAuctions.remove(snapshot);
        });

        buttonBox.getChildren().addAll(
                viewButton,
                removeButton
        );

        rightBox.getChildren().addAll(
                statusLabel,
                endLabel,
                buttonBox
        );

        card.getChildren().addAll(
                leftBox,
                spacer,
                rightBox
        );

        return card;
    }

    private void updateCountdown(
            Label label,
            LocalDateTime endTime
    ) {

        long seconds =
                LocalDateTime.now()
                        .until(
                                endTime,
                                ChronoUnit.SECONDS
                        );

        if (seconds <= 0) {

            label.setText("ENDED");

            label.setStyle("""
                -fx-text-fill: red;
                -fx-font-weight: bold;
            """);

            return;
        }

        long hours =
                seconds / 3600;

        long minutes =
                (seconds % 3600) / 60;

        long secs =
                seconds % 60;

        label.setText(
                String.format(
                        "Ends In : %02d:%02d:%02d",
                        hours,
                        minutes,
                        secs
                )
        );
    }

    private void openAuction(
            AuctionSnapshot snapshot
    ) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/com/bidplaza/ui/AuctionDetail.fxml"
                            )
                    );

            Scene scene =
                    new Scene(
                            loader.load()
                    );

            AppStyles.applyTo(scene);

            AuctionDetailController controller =
                    loader.getController();

            AuctionItem item =
                    new AuctionItem(
                            snapshot.getId(),
                            snapshot.getName(),
                            snapshot.getCategory(),
                            "$" + snapshot.getStartingPrice(),
                            "$" + snapshot.getCurrentPrice(),
                            snapshot.getStatus(),
                            snapshot.getEndTime().toString()
                    );

            controller.setAuction(
                    item,
                    snapshot
            );

            Stage stage =
                    (Stage) rootPane
                            .getScene()
                            .getWindow();

            if (stage.getScene() != null) {
                javafx.scene.Parent rootNode = scene.getRoot();
                scene.setRoot(new javafx.scene.layout.Pane());
                stage.getScene().setRoot(rootNode);
            } else {
                stage.setScene(scene);
            }
            stage.setMaximized(true);
            

            stage.setTitle(
                    "BidPlaza - "
                            + snapshot.getName()
            );

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @FXML
    private void goBack() {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/com/bidplaza/ui/BidderDashboard.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            Scene scene =
                    new Scene(
                            root,
                            1500,
                            850
                    );

            AppStyles.applyTo(scene);

            Stage stage =
                    (Stage) rootPane
                            .getScene()
                            .getWindow();

            if (stage.getScene() != null) {
                javafx.scene.Parent rootNode = scene.getRoot();
                scene.setRoot(new javafx.scene.layout.Pane());
                stage.getScene().setRoot(rootNode);
            } else {
                stage.setScene(scene);
            }
            stage.setMaximized(true);
            

            stage.setTitle(
                    "BidPlaza - Dashboard"
            );

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}