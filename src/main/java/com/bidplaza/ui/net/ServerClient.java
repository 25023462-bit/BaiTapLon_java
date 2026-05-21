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

    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 2000;
    private static final int CONNECT_TIMEOUT_MS = 3000;

    private ServerClient() {}

    public static Message request(Message request) throws IOException, ClassNotFoundException {
        IOException lastException = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try (Socket socket = new Socket()) {
                socket.connect(
                    new InetSocketAddress("localhost", ServerPort.get()),
                    CONNECT_TIMEOUT_MS
                );
                try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                     ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                    out.writeObject(request);
                    out.flush();
                    return (Message) in.readObject();
                }
            } catch (IOException e) {
                lastException = e;
                System.err.println("[ServerClient] Lan thu " + attempt
                    + "/" + MAX_ATTEMPTS + " that bai: " + e.getMessage());

                if (attempt < MAX_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Bi ngat khi cho reconnect", ie);
                    }
                }
            }
        }

        // Hết lần thử → ném lỗi để controller hiện error dialog
        throw new IOException(
            "Khong the ket noi Server sau " + MAX_ATTEMPTS + " lan thu. "
            + "Vui long kiem tra server dang chay.", lastException
        );
    }
}
