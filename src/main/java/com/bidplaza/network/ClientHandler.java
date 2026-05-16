package com.bidplaza.network;

import com.bidplaza.exception.AuctionClosedException;
import com.bidplaza.exception.InvalidBidException;
import com.bidplaza.manager.AuctionManager;
import com.bidplaza.model.Auction;
import com.bidplaza.model.user.Admin;
import com.bidplaza.model.user.Bidder;
import com.bidplaza.model.user.Seller;
import com.bidplaza.model.user.User;

import java.io.*;
import java.net.Socket;

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

        } catch (IOException e) {
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
        switch (message.getType()) {
            case PLACE_BID:
                handlePlaceBid(message);
                break;
            case LOGIN:
                handleLogin(message);
                break;
            default:
                sendMessage(Message.error("Loại message không hợp lệ: " + message.getType()));
        }
    }

    private void handleLogin(Message message) {
        String username = message.getBidderId();
        String info = message.getInfo();
        if (username == null || info == null) {
            sendMessage(Message.loginResponse(false, "Thieu thong tin dang nhap!"));
            return;
        }

        String[] parts = info.split("\\|", 3);
        if (parts.length < 3) {
            sendMessage(Message.loginResponse(false, "Du lieu dang nhap khong hop le!"));
            return;
        }

        String password = parts[0];
        String role = parts[1];
        String action = parts[2];

        if ("REGISTER".equals(action)) {
            register(username, password, role);
        } else {
            login(username, password);
        }
    }

    private void register(String username, String password, String role) {
        if (auctionManager.findUserByUsername(username) != null) {
            sendMessage(Message.loginResponse(false, "Username da ton tai!"));
            return;
        }

        User newUser = switch (role) {
            case "SELLER" -> new Seller(username, password, username + "@mail.com", username);
            case "ADMIN" -> new Admin(username, password, username + "@mail.com");
            default -> new Bidder(username, password, username + "@mail.com");
        };
        auctionManager.addUser(newUser);
        sendMessage(Message.loginResponse(true, "Dang ky thanh cong!"));
    }

    private void login(String username, String password) {
        User user = auctionManager.findUserByUsername(username);
        if (user == null || !user.checkPassword(password)) {
            sendMessage(Message.loginResponse(false, "Sai username hoac mat khau!"));
            return;
        }

        sendMessage(Message.loginResponse(true, "Dang nhap thanh cong!|" + user.getRole()));
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
}
