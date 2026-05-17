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

        welcomeLabel.setText(
                "Welcome back, " + username + "!"
        );
    }

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