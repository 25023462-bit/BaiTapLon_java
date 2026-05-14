package com.bidplaza.network;

import com.bidplaza.exception.AuthenticationException;
import com.bidplaza.exception.AuctionClosedException;
import com.bidplaza.exception.InvalidBidException;
import com.bidplaza.manager.AuctionManager;
import com.bidplaza.manager.UserManager;
import com.bidplaza.model.Auction;
import com.bidplaza.model.user.User;
import com.bidplaza.network.LoginRequest;
import com.bidplaza.network.LoginResponse;

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
    private final UserManager userManager;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientHandler(Socket socket, AuctionManager auctionManager) {
        this.socket = socket;
        this.auctionManager = auctionManager;
        this.userManager = UserManager.getInstance();
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
            case LOGIN:
                handleLogin((LoginRequest) message.getPayload());
                break;
            case PLACE_BID:
                handlePlaceBid(message);
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

        } catch (InvalidBidException e) {
            sendMessage(Message.bidFailed(auction.getId(), e.getMessage()));
        } catch (AuctionClosedException e) {
            sendMessage(Message.bidFailed(auction.getId(), "Phiên đã đóng: " + e.getMessage()));
        }
    }

    private void handleLogin(LoginRequest request) {
        LoginResponse response;

        try {
            User user;
            if (request.isRegister()) {
                user = userManager.register(
                    request.getUsername(),
                    request.getPassword(),
                    request.getRole()
                );
                addUserToAuctionManager(user);
                saveData();
                response = new LoginResponse(true, "Đăng ký thành công!", user);
            } else {
                user = userManager.login(request.getUsername(), request.getPassword());
                response = new LoginResponse(true, "Đăng nhập thành công!", user);
            }
        } catch (AuthenticationException e) {
            response = new LoginResponse(false, e.getMessage(), null);
        }

        sendMessage(new Message(Message.Type.LOGIN, response));
    }

    private void addUserToAuctionManager(User user) {
        try {
            auctionManager.getClass()
                .getMethod("addUser", User.class)
                .invoke(auctionManager, user);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private void saveData() {
        try {
            Class<?> dataStorage = Class.forName("com.bidplaza.storage.DataStorage");
            dataStorage.getMethod("save", AuctionManager.class).invoke(null, auctionManager);
        } catch (ReflectiveOperationException ignored) {
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
