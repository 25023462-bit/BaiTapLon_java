package com.bidplaza;

import com.bidplaza.network.AuctionServer;
import com.bidplaza.network.Message;
import com.bidplaza.network.LoginRequest;
import com.bidplaza.model.user.User;
import com.bidplaza.ui.net.ServerClient;
import com.bidplaza.manager.UserManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerIntegrationTest {

    private static Thread serverThread;
    private static Path tempDataDir;

    @BeforeAll
    public static void setUp() throws Exception {
        tempDataDir = Files.createTempDirectory("bidplaza-server-test");
        System.setProperty("bidplaza.data.dir", tempDataDir.resolve("data").toString());

        User admin = UserManager.getInstance().findByUsername("admin");
        if (admin == null) {
            UserManager.getInstance().register("admin", "admin123", "ADMIN");
        } else {
            admin.setPassword("admin123");
        }

        // Start server in a background thread if port 8080 is not in use
        if (!isPortInUse(8080)) {
            serverThread = new Thread(() -> {
                try {
                    AuctionServer.main(new String[]{});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();
            // Wait 1.5 seconds for server socket to bind and start listening
            Thread.sleep(1500);
        }
    }

    private static boolean isPortInUse(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    @AfterAll
    public static void tearDown() {
        // The server thread is daemon-backed and can keep saving until the test JVM exits.
        // Keep it pointed at the temp directory instead of restoring the real data path.
    }

    @Test
    public void testGetAuctions_shouldReturnList() throws Exception {
        Message message = new Message(Message.Type.GET_AUCTIONS, null);
        Message response = ServerClient.request(message);
        
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertTrue(response.getData() instanceof List);
    }

    @Test
    public void testLoginWithValidCredentials_shouldSucceed() throws Exception {
        LoginRequest req = new LoginRequest("admin", "admin123", "ADMIN", false);
        Message message = new Message(Message.Type.LOGIN, req);
        Message response = ServerClient.request(message);

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertTrue(response.getData() instanceof User);
        User user = (User) response.getData();
        assertEquals("admin", user.getUsername());
    }

    @Test
    public void testLoginWithInvalidCredentials_shouldFail() throws Exception {
        LoginRequest req = new LoginRequest("nonexistent", "wrongpass", "ADMIN", false);
        Message message = new Message(Message.Type.LOGIN, req);
        Message response = ServerClient.request(message);

        assertFalse(response.isSuccess());
    }
}
