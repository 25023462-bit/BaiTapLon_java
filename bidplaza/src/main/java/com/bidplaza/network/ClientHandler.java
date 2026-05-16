package com.bidplaza.network;

import com.bidplaza.exception.AuctionClosedException;
import com.bidplaza.exception.InvalidBidException;
import com.bidplaza.manager.AuctionManager;
import com.bidplaza.model.Auction;

import java.io.*;
import java.net.Socket;
import com.bidplaza.storage.DataStorage;

/**
 * Xử lý 1 client cụ thể - chạy trên thread riêng.
 *
 * Mỗi client kết nối → server tạo 1 ClientHandler mới.
 * ClientHandler liên tục đọc Message từ client,
 * xử lý, rồi gửi kết quả trả về.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final AuctionManager auctionManager;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientHandler(Socket socket, AuctionManager auctionManager) {
        this.socket = socket;
        this.auctionManager = auctionManager;
    }

    @Override
    public void run() {
        try {
            // ObjectOutputStream/InputStream: gửi/nhận Java object qua Socket
            out = new ObjectOutputStream(socket.getOutputStream());
            in  = new ObjectInputStream(socket.getInputStream());

            // Vòng lặp: liên tục đọc message từ client
            while (true) {
                Message message = (Message) in.readObject();
                System.out.println("Nhận từ client: " + message);
                handleMessage(message);
            }

        } catch (EOFException | IOException e) {
            // Client ngắt kết nối
            System.out.println("Client đã ngắt kết nối.");
        } catch (ClassNotFoundException e) {
            System.out.println("Lỗi đọc message: " + e.getMessage());
        } finally {
            AuctionServer.removeClient(this);
            closeConnection();
        }
    }

    /**
     * Xử lý từng loại message từ client.
     */
    private void handleMessage(Message message) {
        System.out.println("Type nhận được: " + message.getType() + " | Class: " + message.getClass().getName());
        switch (message.getType()) {
            case PLACE_BID:
                handlePlaceBid(message);
                break;
            case LOGIN:          // ← thêm
                handleLogin(message);
                break;
            default:
                sendMessage(Message.error("Loại message không hợp lệ: " + message.getType()));
        }
    }

    private void handlePlaceBid(Message message) {
        Auction auction = auctionManager.findById(message.getAuctionId());

        if (auction == null) {
            sendMessage(Message.error("Không tìm thấy phiên: " + message.getAuctionId()));
            return;
        }

        try {
            auction.placeBid(message.getBidderId(), message.getAmount());

            // Gửi xác nhận cho client này
            sendMessage(Message.bidSuccess(auction.getId(), auction.getItem().getCurrentPrice()));

            // Broadcast giá mới cho tất cả client khác
            Message update = Message.auctionUpdate(
                auction.getId(),
                auction.getItem().getCurrentPrice(),
                message.getBidderId()
            );
            AuctionServer.broadcast(update, this);
            // Auto-save sau khi có bid mới
            DataStorage.save(auctionManager);

        } catch (InvalidBidException e) {
            sendMessage(Message.bidFailed(auction.getId(), e.getMessage()));
        } catch (AuctionClosedException e) {
            sendMessage(Message.bidFailed(auction.getId(), "Phiên đã đóng: " + e.getMessage()));
        }
    }

    /**
     * Gửi message đến client này.
     * synchronized: tránh 2 thread cùng ghi vào stream cùng lúc.
     */
    public synchronized void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.out.println("Lỗi gửi message: " + e.getMessage());
        }
    }

    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            // bỏ qua lỗi khi đóng
        }
    }

    private void handleLogin(Message message) {
        String[] parts = message.getInfo().split("\\|");
        String password = parts[0];
        String role     = parts[1];
        String action   = parts[2]; // LOGIN hoặc REGISTER
        String username = message.getBidderId();

        if (action.equals("REGISTER")) {
            // Kiểm tra username đã tồn tại chưa
            if (auctionManager.findUserByUsername(username) != null) {
                sendMessage(Message.loginResponse(false, "Username đã tồn tại!"));
                return;
            }
            // Tạo user mới theo role
            com.bidplaza.model.user.User newUser = switch (role) {
                case "SELLER" -> new com.bidplaza.model.user.Seller(
                        java.util.UUID.randomUUID().toString(), username, username + "@mail.com", password);
                case "ADMIN"  -> new com.bidplaza.model.user.Admin(
                        java.util.UUID.randomUUID().toString(), username, username + "@mail.com", password);
                default       -> new com.bidplaza.model.user.Bidder(
                        java.util.UUID.randomUUID().toString(), username, username + "@mail.com", password);
            };
            auctionManager.addUser(newUser);
            DataStorage.save(auctionManager);
            sendMessage(Message.loginResponse(true, "Đăng ký thành công!"));
        } else {
            // Đăng nhập
            com.bidplaza.model.user.User user = auctionManager.findUserByUsername(username);
            if (user == null || !user.checkPassword(password)) {
                sendMessage(Message.loginResponse(false, "Sai username hoặc mật khẩu!"));
                return;
            }
            sendMessage(Message.loginResponse(true, "Đăng nhập thành công!|" + user.getRole()));
        }
    }
}
