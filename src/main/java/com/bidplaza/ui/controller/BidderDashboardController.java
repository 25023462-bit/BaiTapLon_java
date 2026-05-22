package com.bidplaza.ui.controller;

import com.bidplaza.ui.AppStyles;
import com.bidplaza.ui.model.UserSession;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;
import javafx.scene.control.Label;

import javafx.stage.Stage;

public class BidderDashboardController {

    @FXML
    private Label welcomeLabel;

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
                        getClass().getResource("/com/bidplaza/ui/style.css").toExternalForm()
                    );
                }
            } catch (Exception ignored) {}
        });
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

    // MY BIDS

    @FXML
    private void openMyBids() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/bidplaza/ui/MyBids.fxml"
                    )
            );

            Scene scene = new Scene(loader.load());

            AppStyles.applyTo(scene);

            Stage stage =
                    (Stage) welcomeLabel.getScene().getWindow();

            stage.setScene(scene);

            stage.setTitle("BidPlaza - My Bids");

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // WATCHLIST

    @FXML
    private void openWatchlist() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/bidplaza/ui/Watchlist.fxml"
                    )
            );

            Scene scene = new Scene(loader.load());

            AppStyles.applyTo(scene);

            Stage stage =
                    (Stage) welcomeLabel.getScene().getWindow();

            stage.setScene(scene);

            stage.setTitle("BidPlaza - Watchlist");

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // HISTORY

    @FXML
    private void openHistory() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/bidplaza/ui/History.fxml"
                    )
            );

            Scene scene = new Scene(loader.load());

            AppStyles.applyTo(scene);

            Stage stage =
                    (Stage) welcomeLabel.getScene().getWindow();

            stage.setScene(scene);

            stage.setTitle("BidPlaza - History");

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // PROFILE

    @FXML
    private void openProfile() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/bidplaza/ui/Profile.fxml"
                    )
            );

            Scene scene = new Scene(loader.load());

            AppStyles.applyTo(scene);

            Stage stage =
                    (Stage) welcomeLabel.getScene().getWindow();

            stage.setScene(scene);

            stage.setTitle("BidPlaza - Profile");

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // LOGOUT

    @FXML
    private void handleLogout() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/bidplaza/ui/Login.fxml"
                    )
            );

            Scene scene = new Scene(loader.load());

            AppStyles.applyTo(scene);

            Stage stage =
                    (Stage) welcomeLabel.getScene().getWindow();

            stage.setScene(scene);

            stage.setTitle("BidPlaza - Login");

            stage.show();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}