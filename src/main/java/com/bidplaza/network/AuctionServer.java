package com.bidplaza.network;

import com.bidplaza.factory.ItemFactory;
import com.bidplaza.manager.AuctionManager;
import com.bidplaza.model.Auction;
import com.bidplaza.model.item.Item;
import com.bidplaza.storage.DataStorage;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Server chính - lắng nghe kết nối từ nhiều Client đồng thời.
 *
 * Fix Phase 1.3:
 * - BUG CŨ: broadcast() bỏ qua sender (client != sender) nên client vừa đặt giá
 *   không nhận được AUCTION_UPDATE → UI không tự refresh giá mới nhất.
 * - FIX: broadcast gửi cho TẤT CẢ client (kể cả sender).
 *   ClientHandler tự xử lý update khi nhận AUCTION_UPDATE.
 * - THÊM: xóa client khỏi danh sách nếu gửi thất bại (client đã ngắt kết nối).
 */
public class AuctionServer {

    private static final int PORT = 8080;

    // Thread-safe list: đọc nhiều, ghi ít
    private static final List<ClientHandler> connectedClients = new CopyOnWriteArrayList<>();

    private static AuctionManager auctionManager;

    private static final java.util.Map<String, ClientHandler> userClientMap =
            new java.util.concurrent.ConcurrentHashMap<>();

    public static void registerClient(String userId, ClientHandler handler) {
        userClientMap.put(userId, handler);
    }

    public static void unregisterClient(String userId) {
        userClientMap.remove(userId);
    }

    public static ClientHandler findClientByUserId(String userId) {
        return userClientMap.get(userId);
    }

    public static void main(String[] args) throws IOException {
        // Load data từ storage
        auctionManager = DataStorage.load();

        // Đồng bộ Singleton với instance vừa load
        try {
            java.lang.reflect.Field instanceField =
                AuctionManager.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, auctionManager);
        } catch (Exception ignored) {}

        com.bidplaza.manager.UserManager.getInstance();

        // AuctionTimer kiểm tra phiên hết hạn mỗi 10 giây
        ScheduledExecutorService timerScheduler = Executors.newSingleThreadScheduledExecutor();
        AuctionTimer timerTask = new AuctionTimer(auctionManager, new DataStorage());
        timerScheduler.scheduleAtFixedRate(timerTask, 0, 10, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[Server] Dang tat scheduler...");
            timerScheduler.shutdown();
            try {
                if (!timerScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    timerScheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                timerScheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("[Server] Scheduler da dung gracefully.");
        }));

        // Tạo sẵn 1 phiên đấu giá để test
        Item phone = ItemFactory.create(
            "electronics", "iPhone 15 Pro", "Moi 100%",
            1000.0, LocalDateTime.now(),
            LocalDateTime.now().plusHours(1), "seller-001"
        );
        Auction auction = auctionManager.createAuction(phone);
        auction.start();
        System.out.println("Phien dau gia tao san: " + auction.getId());

        auction.registerAutoBid("bot-vip-1", 1500.0, 50.0);
        auction.registerAutoBid("bot-vip-2", 2000.0, 100.0);
        System.out.println("Da them bot-vip-1 (max $1500, inc $50) va bot-vip-2 (max $2000, inc $100)");

        ExecutorService pool = Executors.newFixedThreadPool(10);

        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server dang chay tai cong " + PORT + "...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client moi ket noi: " + clientSocket.getInetAddress());

            ClientHandler handler = new ClientHandler(clientSocket, auctionManager);
            connectedClients.add(handler);
            pool.submit(handler);
        }
    }

    /**
     * Gửi message đến TẤT CẢ client đang kết nối (kể cả sender).
     *
     * Lý do gửi cả sender:
     * - Sender cần nhận AUCTION_UPDATE để UI cập nhật giá mới nhất ngay lập tức.
     * - Không có duplicate vì server chỉ broadcast 1 lần sau khi bid thành công.
     *
     * @param message  message cần broadcast
     * @param sender   không dùng nữa (giữ tham số để tương thích API cũ)
     */
    public static synchronized void broadcast(Message message, ClientHandler sender) {
        System.out.println("[AuctionServer] Broadcasting " + message.getType()
            + " den " + connectedClients.size() + " clients");

        List<ClientHandler> toRemove = new java.util.ArrayList<>();
        for (ClientHandler client : connectedClients) {
            try {
                client.sendMessage(message);
            } catch (Exception e) {
                // Client ngắt kết nối → đánh dấu để xóa
                System.err.println("[AuctionServer] Gui that bai, xoa client: " + e.getMessage());
                toRemove.add(client);
            }
        }
        connectedClients.removeAll(toRemove);
    }

    public static void removeClient(ClientHandler handler) {
        connectedClients.remove(handler);
        System.out.println("Client ngat ket noi. Con lai: " + connectedClients.size());
    }

    public static AuctionManager getAuctionManager() {
        return auctionManager;
    }
}
