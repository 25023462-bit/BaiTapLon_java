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
import com.bidplaza.storage.DataStorage;
import com.bidplaza.model.user.Bidder;
import com.bidplaza.model.user.Seller;

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
        auctionManager = DataStorage.load();

        // Tạo dữ liệu mẫu chỉ khi lần đầu chạy (chưa có dữ liệu)
        if (auctionManager.getAllAuctions().isEmpty()) {
            // User mẫu
            Bidder bidder1 = new Bidder("b001", "Alice", "alice@mail.com", "123");
            Bidder bidder2 = new Bidder("b002", "Bob", "bob@mail.com", "123");
            Seller seller1 = new Seller("s001", "Shop ABC", "shop@mail.com", "123");
            auctionManager.addUser(bidder1);
            auctionManager.addUser(bidder2);
            auctionManager.addUser(seller1);

            // Phiên đấu giá mẫu
            Item phone = ItemFactory.create("electronics", "i001",
                    "iPhone 15 Pro", "Mới 100%", 1000.0,
                    LocalDateTime.now(), LocalDateTime.now().plusHours(1), "s001");
            Item laptop = ItemFactory.create("electronics", "i002",
                    "Laptop Dell", "Core i7", 500.0,
                    LocalDateTime.now(), LocalDateTime.now().plusHours(2), "s001");

            Auction auction1 = auctionManager.createAuction(phone);
            Auction auction2 = auctionManager.createAuction(laptop);
            auction1.start();
            auction2.start();

            DataStorage.save(auctionManager);
            System.out.println("Đã tạo dữ liệu mẫu!");
        } else {
            System.out.println("Đã load " + auctionManager.getAllAuctions().size() + " phiên đấu giá.");
        }

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
