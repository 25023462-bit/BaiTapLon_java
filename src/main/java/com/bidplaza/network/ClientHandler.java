package com.bidplaza.network;

import com.bidplaza.exception.AuthenticationException;
import com.bidplaza.exception.AuctionClosedException;
import com.bidplaza.exception.InvalidBidException;
import com.bidplaza.manager.AuctionManager;
import com.bidplaza.manager.UserManager;
import com.bidplaza.model.Auction;
import com.bidplaza.model.user.User;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Handles one connected client on a dedicated thread.
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
            out = new ObjectOutputStream(socket.getOutputStream());
            in  = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Message message = (Message) in.readObject();
                System.out.println("Nhan tu client: " + message);
                handleMessage(message);
            }
        } catch (IOException e) {
            System.out.println("Client da ngat ket noi.");
        } catch (ClassNotFoundException e) {
            System.out.println("Loi doc message: " + e.getMessage());
        } finally {
            AuctionServer.removeClient(this);
            closeConnection();
        }
    }

    private void handleMessage(Message message) {
        switch (message.getType()) {
            case LOGIN:
                handleLogin((LoginRequest) message.getPayload());
                break;
            case PLACE_BID:
                handlePlaceBid(message);
                break;
            case LIST_AUCTIONS:
            case GET_AUCTION_LIST:
                handleListAuctions();
                break;
            case CREATE_AUCTION:
                handleCreateAuction((CreateAuctionRequest) message.getPayload());
                break;
            case FINISH_AUCTION:
                handleFinishAuction(message.getAuctionId());
                break;
            default:
                sendMessage(Message.error("Loai message khong hop le: " + message.getType()));
        }
    }

    private void handleLogin(LoginRequest request) {
        if (request == null) {
            sendMessage(Message.loginResponse(false, "Thieu thong tin dang nhap!"));
            return;
        }

        try {
            User user;
            if (request.isRegister()) {
                user = userManager.register(
                    request.getUsername(),
                    request.getPassword(),
                    request.getRole()
                );
                auctionManager.addUser(user);
                saveData();
                sendMessage(Message.loginResponse(true, "Dang ky thanh cong!", user));
            } else {
                user = userManager.login(request.getUsername(), request.getPassword());
                sendMessage(Message.loginResponse(true, "Dang nhap thanh cong!", user));
            }
        } catch (AuthenticationException e) {
            sendMessage(Message.loginResponse(false, e.getMessage()));
        }
    }

    private void handlePlaceBid(Message message) {
        Auction auction = auctionManager.findById(message.getAuctionId());

        if (auction == null) {
            sendMessage(Message.error("Khong tim thay phien: " + message.getAuctionId()));
            return;
        }

        try {
            auction.placeBid(message.getBidderId(), message.getAmount());
            sendMessage(Message.bidSuccess(auction.getId(), auction.getItem().getCurrentPrice()));

            Message update = new Message(
                Message.Type.AUCTION_UPDATE,
                AuctionSnapshot.from(auction)
            );
            AuctionServer.broadcast(update, null);
            saveData();
        } catch (InvalidBidException e) {
            sendMessage(Message.bidFailed(auction.getId(), e.getMessage()));
        } catch (AuctionClosedException e) {
            sendMessage(Message.bidFailed(auction.getId(), "Phien da dong: " + e.getMessage()));
        }
    }

    private void handleListAuctions() {
        java.util.List<AuctionSnapshot> snapshots = new java.util.ArrayList<>();
        for (Auction auction : auctionManager.getAllAuctions()) {
            snapshots.add(AuctionSnapshot.from(auction));
        }
        sendMessage(new Message(Message.Type.LIST_AUCTIONS, snapshots));
    }

    private void handleCreateAuction(CreateAuctionRequest req) {
        if (req == null) {
            sendMessage(Message.error("Thieu thong tin tao phien!"));
            return;
        }

        try {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            com.bidplaza.model.item.Item item = com.bidplaza.factory.ItemFactory.create(
                req.getCategory(),
                req.getName(),
                req.getDescription(),
                req.getStartingPrice(),
                now,
                now.plusHours(req.getDurationHours()),
                req.getSellerId()
            );
            Auction auction = auctionManager.createAuction(item);
            auction.start();
            saveData();
            sendMessage(new Message(Message.Type.CREATE_AUCTION, AuctionSnapshot.from(auction)));
        } catch (Exception e) {
            sendMessage(Message.error("Loi tao phien: " + e.getMessage()));
        }
    }

    private void handleFinishAuction(String auctionId) {
        Auction auction = auctionManager.findById(auctionId);
        if (auction == null) {
            sendMessage(Message.error("Khong tim thay phien dau gia: " + auctionId));
            return;
        }

        auction.finish();
        saveData();
        Message update = new Message(Message.Type.AUCTION_UPDATE, AuctionSnapshot.from(auction));
        AuctionServer.broadcast(update, this);
    }

    public synchronized void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.out.println("Loi gui message: " + e.getMessage());
        }
    }

    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            // Ignore close failures.
        }
    }

    private void saveData() {
        try {
            Class<?> dataStorage = Class.forName("com.bidplaza.storage.DataStorage");
            dataStorage.getMethod("save", AuctionManager.class).invoke(null, auctionManager);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
