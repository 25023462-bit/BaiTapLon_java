package com.bidplaza.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point của JavaFX Client.
 *
 * Mở màn hình Login đầu tiên.
 * Sau khi đăng nhập → tự động chuyển sang màn hình phù hợp.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/bidplaza/ui/Login.fxml"));
        Scene scene = new Scene(loader.load(), 500, 520);
        stage.setTitle("BidPlaza - Đăng nhập");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
