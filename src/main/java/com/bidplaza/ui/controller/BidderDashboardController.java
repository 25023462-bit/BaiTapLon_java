package com.bidplaza.ui.controller;

import com.bidplaza.network.AuctionSnapshot;
import com.bidplaza.network.Message;
import com.bidplaza.ui.AppStyles;
import com.bidplaza.ui.model.AuctionItem;
import com.bidplaza.ui.model.UserSession;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.geometry.Pos;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

public class BidderDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private VBox liveAuctionContainer;

    // RIGHT PANEL LABELS

    @FXML
    private Label liveStatusLabel;

    @FXML
    private Label totalAuctionsLabel;

    @FXML
    private Label highestBidLabel;

    @FXML
    private Label endingSoonLabel;

    private Timeline refreshTimeline;

    @FXML
    public void initialize() {

        String username =
                UserSession.getInstance().getUsername();

        if (welcomeLabel != null) {

            welcomeLabel.setText(
                    "Welcome back, " + username + "!"
            );
        }

        javafx.application.Platform.runLater(() -> {

            try {

                Scene scene = welcomeLabel.getScene();

                if (scene != null) {

                    scene.getStylesheets().add(
                            getClass()
                                    .getResource("/com/bidplaza/ui/style.css")
                                    .toExternalForm()
                    );
                }

            } catch (Exception ignored) {}
        });

        loadLiveAuctions();

        startAutoRefresh();
    }

    // LOAD LIVE AUCTIONS

    private void loadLiveAuctions() {

        try {

            liveAuctionContainer.getChildren().clear();

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

                List<AuctionSnapshot> liveAuctions =
                        new ArrayList<>();

                if (snapshots != null) {

                    for (Object obj : snapshots) {

                        if (obj instanceof AuctionSnapshot snapshot) {

                            liveAuctions.add(snapshot);

                            HBox card =
                                    createAuctionCard(snapshot);

                            liveAuctionContainer
                                    .getChildren()
                                    .add(card);
                        }
                    }
                }

                updateRightPanel(liveAuctions);
            }

        } catch (Exception e) {

            e.printStackTrace();

            Label error =
                    new Label(
                            "FAILED TO LOAD LIVE AUCTIONS"
                    );

            error.setStyle("""
                    -fx-text-fill: red;
                    -fx-font-size: 20;
                    -fx-font-weight: bold;
                    """);

            liveAuctionContainer
                    .getChildren()
                    .add(error);
        }
    }

    // UPDATE RIGHT PANEL

    private void updateRightPanel(
            List<AuctionSnapshot> auctions
    ) {

        if (liveStatusLabel != null) {

            liveStatusLabel.setText("ONLINE");
        }

        if (totalAuctionsLabel != null) {

            totalAuctionsLabel.setText(
                    String.valueOf(auctions.size())
            );
        }

        double highestBid = 0;

        LocalDateTime nearestEnd = null;

        for (AuctionSnapshot snapshot : auctions) {

            if (snapshot.getCurrentPrice()
                    > highestBid) {

                highestBid =
                        snapshot.getCurrentPrice();
            }

            if (nearestEnd == null ||
                    snapshot.getEndTime()
                            .isBefore(nearestEnd)) {

                nearestEnd =
                        snapshot.getEndTime();
            }
        }

        if (highestBidLabel != null) {

            highestBidLabel.setText(
                    "$" + highestBid
            );
        }

        if (endingSoonLabel != null) {

            if (nearestEnd != null) {

                long minutes =
                        LocalDateTime.now()
                                .until(
                                        nearestEnd,
                                        ChronoUnit.MINUTES
                                );

                endingSoonLabel.setText(
                        minutes + " mins"
                );

            } else {

                endingSoonLabel.setText(
                        "--"
                );
            }
        }
    }

    // CREATE CARD

    private HBox createAuctionCard(
            AuctionSnapshot snapshot
    ) {

        HBox card = new HBox();

        card.setSpacing(25);

        card.setAlignment(Pos.CENTER_LEFT);

        card.setPrefWidth(720);

        card.setPrefHeight(120);

        card.setStyle("""
            -fx-background-color: rgba(20,20,45,0.82);

            -fx-background-radius: 20;

            -fx-border-color: #00e5ff;

            -fx-border-radius: 20;

            -fx-padding: 20;
        """);

        VBox infoBox = new VBox();

        infoBox.setSpacing(12);

        Label nameLabel =
                new Label(snapshot.getName());

        nameLabel.setStyle("""
            -fx-text-fill: white;
            -fx-font-size: 24;
            -fx-font-weight: bold;
        """);

        Label bidLabel =
                new Label(
                        "Current Bid: $"
                                + snapshot.getCurrentPrice()
                );

        bidLabel.setStyle("""
            -fx-text-fill: #00e5ff;
            -fx-font-size: 16;
        """);

        Label timeLabel =
                new Label();

        timeLabel.setStyle("""
            -fx-text-fill: #ff4dff;
            -fx-font-size: 16;
        """);

        updateCountdown(
                timeLabel,
                snapshot.getEndTime()
        );

        Timeline countdown =
                new Timeline(
                        new KeyFrame(
                                Duration.seconds(1),
                                e -> updateCountdown(
                                        timeLabel,
                                        snapshot.getEndTime()
                                )
                        )
                );

        countdown.setCycleCount(
                Timeline.INDEFINITE
        );

        countdown.play();

        infoBox.getChildren().addAll(
                nameLabel,
                bidLabel,
                timeLabel
        );

        Region spacer = new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Button bidButton =
                new Button("BID NOW");

        bidButton.setPrefWidth(140);

        bidButton.setPrefHeight(45);

        bidButton.setStyle("""
            -fx-background-color: rgba(0,229,255,0.15);

            -fx-border-color: #00e5ff;

            -fx-border-radius: 15;

            -fx-background-radius: 15;

            -fx-text-fill: white;

            -fx-font-weight: bold;

            -fx-cursor: hand;
        """);

        bidButton.setOnAction(e -> {

            try {

                FXMLLoader loader =
                        new FXMLLoader(
                                getClass().getResource(
                                        "/com/bidplaza/ui/AuctionDetail.fxml"
                                )
                        );

                Scene scene =
                        new Scene(loader.load());

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

                controller.setAuction(item);

                Stage stage =
                        (Stage) welcomeLabel
                                .getScene()
                                .getWindow();

                stage.setScene(scene);

                stage.setTitle(
                        "BidPlaza - "
                                + snapshot.getName()
                );

                stage.show();

            } catch (Exception ex) {

                ex.printStackTrace();
            }
        });

        card.getChildren().addAll(
                infoBox,
                spacer,
                bidButton
        );

        return card;
    }

    // COUNTDOWN

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
                -fx-font-size: 16;
                -fx-font-weight: bold;
            """);

            return;
        }

        long hours = seconds / 3600;

        long minutes =
                (seconds % 3600) / 60;

        long secs =
                seconds % 60;

        label.setText(
                String.format(
                        "Time Left: %02d:%02d:%02d",
                        hours,
                        minutes,
                        secs
                )
        );
    }

    // AUTO REFRESH

    private void startAutoRefresh() {

        refreshTimeline =
                new Timeline(
                        new KeyFrame(
                                Duration.seconds(5),
                                e -> loadLiveAuctions()
                        )
                );

        refreshTimeline.setCycleCount(
                Timeline.INDEFINITE
        );

        refreshTimeline.play();
    }

    // AUCTIONS

    @FXML
    private void openAuctions() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/bidplaza/ui/AuctionList.fxml"
                    )
            );

            Scene scene = new Scene(loader.load());

            AppStyles.applyTo(scene);

            Stage stage =
                    (Stage) welcomeLabel.getScene().getWindow();

            stage.setScene(scene);

            stage.setTitle("BidPlaza - Auctions");

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    @FXML
    private void openMyBids() {}

    @FXML
    private void openWatchlist() {}

    @FXML
    private void openHistory() {}

    @FXML
    private void openProfile() {}

    @FXML
    private void handleLogout() {}
}