package com.bidplaza;

import com.bidplaza.ui.AppStyles;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(
                getClass().getResource("/com/bidplaza/ui/Login.fxml")
        );

        Scene scene = new Scene(root, 1200, 700);
        AppStyles.applyTo(scene);

        stage.setTitle("BidPlaza - Online Auction System");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
