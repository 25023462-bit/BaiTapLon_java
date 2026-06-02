package com.bidplaza.ui;

import com.bidplaza.ui.AppStyles;
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
        AppStyles.applyTo(scene);
        stage.setTitle("BidPlaza - Đăng nhập");
        if (stage.getScene() != null) {
                javafx.scene.Parent rootNode = scene.getRoot();
                scene.setRoot(new javafx.scene.layout.Pane());
                stage.getScene().setRoot(rootNode);
            } else {
                stage.setScene(scene);
            }
            stage.setMaximized(true);
            
        
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
