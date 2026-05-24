package com.bidplaza.ui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;:

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
@Disabled("JavaFX requires display - cannot run in headless CI")
class FxmlLoadTest {

    @Test
    void applicationFxmlFilesLoad() throws Exception {
        configureFastClientFailure();
        try {
            startPlatform();

            List<String> failures = new ArrayList<>();
            for (String path : fxmlFiles()) {
                Throwable error = loadOnFxThread(path);
                if (error != null) {
                    failures.add(path + " -> " + error.getClass().getSimpleName()
                        + ": " + error.getMessage());
                }
            }

            assertTrue(failures.isEmpty(), String.join(System.lineSeparator(), failures));
        } finally {
            clearFastClientFailureConfig();
        }
    }

    private Throwable loadOnFxThread(String path) throws Exception {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            try {
                URL resource = getClass().getResource(path);
                assertNotNull(resource, "Missing FXML resource: " + path);
                new FXMLLoader(resource).load();
            } catch (Throwable t) {
                error.set(t);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Timed out loading " + path);
        return error.get();
    }

    private void startPlatform() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
        }
    }

    private void configureFastClientFailure() {
        System.setProperty("bidplaza.port", "65534");
        System.setProperty("bidplaza.client.maxAttempts", "1");
        System.setProperty("bidplaza.client.retryDelayMs", "0");
        System.setProperty("bidplaza.client.connectTimeoutMs", "100");
    }

    private void clearFastClientFailureConfig() {
        System.clearProperty("bidplaza.port");
        System.clearProperty("bidplaza.client.maxAttempts");
        System.clearProperty("bidplaza.client.retryDelayMs");
        System.clearProperty("bidplaza.client.connectTimeoutMs");
    }

    private String[] fxmlFiles() {
        return new String[] {
            "/com/bidplaza/ui/AdminDashboard.fxml",
            "/com/bidplaza/ui/AuctionDetail.fxml",
            "/com/bidplaza/ui/AuctionList.fxml",
            "/com/bidplaza/ui/BidderDashboard.fxml",
            "/com/bidplaza/ui/Deposit.fxml",
            "/com/bidplaza/ui/History.fxml",
            "/com/bidplaza/ui/Login.fxml",
            "/com/bidplaza/ui/MyBids.fxml",
            "/com/bidplaza/ui/Profile.fxml",
            "/com/bidplaza/ui/Register.fxml",
            "/com/bidplaza/ui/SellerDashboard.fxml",
            "/com/bidplaza/ui/Watchlist.fxml",
            "/com/bidplaza/ui/Welcome.fxml"
        };
    }
}
