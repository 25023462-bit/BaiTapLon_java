package com.bidplaza.ui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNull;

class FxmlLoadTest {

    @Test
    void sellerDashboardLoads() throws Exception {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
        }

        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/bidplaza/ui/SellerDashboard.fxml"));
                loader.load();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);
        assertNull(error.get(), error.get() != null ? error.get().getMessage() : null);
    }
}
