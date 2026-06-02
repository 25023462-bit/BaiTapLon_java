package com.bidplaza.ui;

import javafx.scene.Scene;

public final class AppStyles {
    private static final String STYLESHEET = "/com/bidplaza/ui/app.css";

    private AppStyles() {
    }

    public static void applyTo(Scene scene) {
        String css = AppStyles.class.getResource(STYLESHEET).toExternalForm();
        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }
    }
}
