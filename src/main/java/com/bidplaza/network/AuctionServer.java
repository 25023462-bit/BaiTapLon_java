package com.bidplaza.network;

import com.bidplaza.factory.ItemFactory;
import com.bidplaza.manager.AuctionManager;
import com.bidplaza.model.Auction;
import com.bidplaza.model.item.Item;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server chính - lắng nghe kết nối từ nhiều Client đồng thời.
 *
 * Cách hoạt động:
 * 1. Server mở cổng (port) 8080, chờ client kết nối
 * 2. Mỗi client kết nối → tạo 1 thread riêng xử lý
 * 3. Nhận Message từ client → xử lý → gửi lại kết quả
 * 4. Broadcast giá mới cho TẤT CẢ client đang kết nối
 */
public class AuctionServer {

    private static final int PORT = 8080;

    // Danh sách tất cả client đang kết nối
    // CopyOnWriteArrayList: thread-safe khi nhiều thread cùng đọc/ghi
    private static final List<ClientHandler> connectedClients = new CopyOnWriteArrayList<>();

    private static AuctionManager auctionManager;

    public static void main(String[] args) throws IOException {
        auctionManager = AuctionManager.getInstance();
        com.bidplaza.manager.UserManager.getInstance();

        // Tạo sẵn 1 phiên đấu giá để test
        Item phone = ItemFactory.create(
            "electronics", "iPhone 15 Pro", "Mới 100%",
            1000.0, LocalDateTime.now(),
            LocalDateTime.now().plusHours(1), "seller-001"
        );
        Auction auction = auctionManager.createAuction(phone);
        auction.start();
        System.out.println("Phiên đấu giá tạo sẵn: " + auction.getId());
        
        // Thêm một số Auto-Bidder để demo logic đấu giá tự động
        auction.registerAutoBid("bot-vip-1", 1500.0, 50.0);
        auction.registerAutoBid("bot-vip-2", 2000.0, 100.0);
        System.out.println("Đã thêm bot-vip-1 (max $1500, inc $50) và bot-vip-2 (max $2000, inc $100)");

        // Thread pool: tối đa 10 client cùng lúc
        ExecutorService pool = Executors.newFixedThreadPool(10);

        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server đang chạy tại cổng " + PORT + "...");

        while (true) {
            // Chờ client kết nối
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client mới kết nối: " + clientSocket.getInetAddress());

            // Tạo handler cho client này, chạy trên thread riêng
            ClientHandler handler = new ClientHandler(clientSocket, auctionManager);
            connectedClients.add(handler);
            pool.submit(handler);
        }
    }

    /**
     * Gửi message đến TẤT CẢ client đang kết nối.
     * Dùng khi có bid mới → thông báo realtime cho mọi người.
     */
    public static void broadcast(Message message, ClientHandler sender) {
        for (ClientHandler client : connectedClients) {
            if (client != sender) { // không gửi lại cho người vừa gửi
                client.sendMessage(message);
            }
        }
    }

    public static void removeClient(ClientHandler handler) {
        connectedClients.remove(handler);
        System.out.println("Client ngắt kết nối. Còn lại: " + connectedClients.size());
    }

    public static AuctionManager getAuctionManager() {
        return auctionManager;
    }
}
