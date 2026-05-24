package com.bidplaza.ui.net;

import com.bidplaza.network.Message;
import com.bidplaza.network.ServerPort;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Gửi request đến Server và nhận response.
 *
 * Fix Phase 1.1:
 * - Thêm reconnect logic: thử lại tối đa 3 lần khi server không phản hồi
 * - Thêm timeout kết nối 3 giây để tránh treo UI vô thời hạn
 * - Ném IOException rõ ràng sau khi hết số lần thử để UI hiện error dialog
 */
public final class ServerClient {

    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_RETRY_DELAY_MS = 2000;
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 3000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 15000;

    private ServerClient() {}

    public static Message request(Message request) throws IOException, ClassNotFoundException {
        IOException lastException = null;
        int maxAttempts = intProperty("bidplaza.client.maxAttempts", DEFAULT_MAX_ATTEMPTS);
        long retryDelayMs = longProperty("bidplaza.client.retryDelayMs", DEFAULT_RETRY_DELAY_MS);
        int connectTimeoutMs = intProperty(
            "bidplaza.client.connectTimeoutMs", DEFAULT_CONNECT_TIMEOUT_MS);
        int readTimeoutMs = intProperty("bidplaza.client.readTimeoutMs", DEFAULT_READ_TIMEOUT_MS);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try (Socket socket = new Socket()) {
                socket.setSoTimeout(readTimeoutMs);
                socket.connect(
                    new InetSocketAddress("localhost", ServerPort.get()),
                    connectTimeoutMs
                );
                try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                    out.writeObject(request);
                    out.flush();
                    try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                        return (Message) in.readObject();
                    }
                }
            } catch (IOException e) {
                lastException = e;
                System.err.println("[ServerClient] Lan thu " + attempt
                    + "/" + maxAttempts + " that bai: " + e.getMessage());

                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Bi ngat khi cho reconnect", ie);
                    }
                }
            }
        }

        // Hết lần thử → ném lỗi để controller hiện error dialog
        throw new IOException(
            "Khong the ket noi Server sau " + maxAttempts + " lan thu. "
            + "Vui long kiem tra server dang chay.", lastException
        );
    }

    public static void sendAsync(Message msg) {
        new Thread(() -> {
            try {
                Socket socket = new Socket("localhost", ServerPort.get());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.writeObject(msg);
                out.flush();
                socket.close();
            } catch (IOException e) {
                System.err.println("[ServerClient] sendAsync failed: " + e.getMessage());
            }
        }).start();
    }

    private static int intProperty(String key, int defaultValue) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static long longProperty(String key, long defaultValue) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Math.max(0, Long.parseLong(value));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
