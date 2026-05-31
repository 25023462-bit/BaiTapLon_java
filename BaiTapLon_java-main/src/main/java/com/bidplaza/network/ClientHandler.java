package com.bidplaza.network;

import com.bidplaza.exception.AuthenticationException;
import com.bidplaza.exception.AuctionClosedException;
import com.bidplaza.exception.InvalidBidException;
import com.bidplaza.manager.AuctionManager;
import com.bidplaza.manager.UserManager;
import com.bidplaza.model.Auction;
import com.bidplaza.model.BidTransaction;
import com.bidplaza.model.Notification;
import com.bidplaza.model.user.Bidder;
import com.bidplaza.model.user.Seller;
import com.bidplaza.model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Xử lý 1 client kết nối trên thread riêng.
 *
 * Phase 3: thêm case REGISTER_AUTO_BID để server đăng ký auto-bid
 * cho Auction tương ứng.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final AuctionManager auctionManager;
    private final UserManager userManager;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String currentAuctionId;
    private String currentUserId;

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
            case LOGIN             -> handleLogin((LoginRequest) message.getPayload());
            case PLACE_BID         -> handlePlaceBid(message);
            case LIST_AUCTIONS,
                 GET_AUCTIONS,
                 GET_AUCTION_LIST  -> handleListAuctions();
            case CREATE_AUCTION    -> handleCreateAuction(
                                        (CreateAuctionRequest) message.getPayload());
            case FINISH_AUCTION    -> handleFinishAuction(message.getAuctionId());
            case REGISTER_AUTO_BID -> handleRegisterAutoBid(       // Phase 3
                                        (AutoBidRequest) message.getPayload());
            case DEPOSIT           -> handleDeposit(message);
            case GET_AUCTION_HISTORY -> handleGetAuctionHistory();
            case JOIN_AUCTION      -> {
                AuctionServer.joinRoom(message.getAuctionId(), this);
                this.currentAuctionId = message.getAuctionId();
                this.currentUserId = message.getBidderId();
                if (currentUserId != null) {
                    AuctionServer.registerClient(currentUserId, this);
                }
            }
            case LEAVE_AUCTION     -> AuctionServer.leaveRoom(message.getAuctionId(), this);
            case CHAT_MESSAGE      -> handleChatMessage(message);
            case SUBMIT_REVIEW     -> handleSubmitReview(message);
            case GET_SYSTEM_STATS  -> handleGetSystemStats();
            case GET_ALL_USERS     -> handleGetAllUsers();
            case BAN_USER          -> handleBanUser(message);
            case ADMIN_FORCE_CLOSE -> handleForceClose(message);
            case GET_PROFILE       -> handleGetProfile(message);
            case UPDATE_PASSWORD   -> handleUpdatePassword(message);
            case GET_NOTIFICATIONS -> handleGetNotifications(message);
            case MARK_NOTIFICATIONS_READ -> handleMarkNotificationsRead(message);
            default                -> sendMessage(
                                        Message.error("Loai message khong hop le: "
                                            + message.getType()));
        }
    }

    // ── Handlers ─────────────────────────────────────────────────

    private void handleLogin(LoginRequest request) {
        if (request == null) {
            sendMessage(Message.loginResponse(false, "Thieu thong tin dang nhap!"));
            return;
        }
        try {
            User user;
            if (request.isRegister()) {
                user = userManager.register(
                    request.getUsername(), request.getPassword(), request.getRole());
                auctionManager.addUser(user);
                saveData();
                sendMessage(Message.loginResponse(true, "Dang ky thanh cong!", user));
            } else {
                user = userManager.login(request.getUsername(), request.getPassword());
                currentUserId = user.getId();
                AuctionServer.registerClient(currentUserId, this);
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
            sendMessage(Message.bidSuccess(auction.getId(),
                auction.getItem().getCurrentPrice()));

            AuctionServer.broadcast(
                Message.auctionUpdate(auction.getId(),
                    auction.getItem().getCurrentPrice(),
                    auction.getWinnerId(),
                    AuctionSnapshot.from(auction)),
                null
            );
            // Outbid notification
            String previousBidder = auction.getPreviousHighestBidder();
            String currentBidder = message.getBidderId();
            if (previousBidder != null && !previousBidder.equals(currentBidder)) {
                notifyUser(previousBidder, new Notification(
                    "Ban bi vuot gia",
                    "Ban vua bi vuot gia tai phien: " + auction.getItem().getName(),
                    "OUTBID"));
            }
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
                req.getCategory(), req.getName(), req.getDescription(),
                req.getStartingPrice(), now,
                now.plusHours(req.getDurationHours()), req.getSellerId()
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
            sendMessage(Message.error("Khong tim thay phien: " + auctionId));
            return;
        }
        auction.finish();
        saveData();
        notifyAuctionWinner(auction);
        AuctionServer.broadcast(
            new Message(Message.Type.AUCTION_UPDATE, AuctionSnapshot.from(auction)),
            this
        );
    }

    /**
     * Phase 3: đăng ký auto-bid cho auction.
     * Server gọi auction.registerAutoBid() và xác nhận lại cho client.
     */
    private void handleRegisterAutoBid(AutoBidRequest req) {
        if (req == null) {
            sendMessage(Message.error("Thieu thong tin auto-bid!"));
            return;
        }

        Auction auction = auctionManager.findById(req.getAuctionId());
        if (auction == null) {
            sendMessage(new Message(Message.Type.AUTO_BID_FAILED,
                null, null, 0, "Khong tim thay phien: " + req.getAuctionId()));
            return;
        }
        if (auction.getStatus() != Auction.Status.RUNNING) {
            sendMessage(new Message(Message.Type.AUTO_BID_FAILED,
                null, null, 0, "Phien khong o trang thai RUNNING"));
            return;
        }

        auction.registerAutoBid(req.getBidderId(), req.getMaxBid(), req.getIncrement());

        System.out.println("[Server] Auto-bid da dang ky: " + req.getBidderId()
            + " max=$" + req.getMaxBid() + " inc=$" + req.getIncrement());

        sendMessage(new Message(Message.Type.AUTO_BID_SUCCESS,
            null, null, req.getMaxBid(),
            "Da kich hoat auto-bid den $" + req.getMaxBid()));
    }

    private void handleDeposit(Message message) {
        if (!(message.getPayload() instanceof DepositRequest req)) {
            sendMessage(new Message(Message.Type.DEPOSIT_FAILED, null, null, 0,
                "Thieu thong tin nap tien"));
            return;
        }
        User user = userManager.findById(req.getUserId());
        if (user instanceof Bidder bidder) {
            if (req.getAmount() <= 0) {
                sendMessage(new Message(Message.Type.DEPOSIT_FAILED, null, null, 0,
                    "So tien phai lon hon 0"));
                return;
            }
            bidder.deposit(req.getAmount());
            saveData();
            sendMessage(new Message(Message.Type.DEPOSIT_SUCCESS, null, null,
                bidder.getBalance(), "Nap tien thanh cong"));
        } else {
            sendMessage(new Message(Message.Type.DEPOSIT_FAILED, null, null, 0,
                "Chi tai khoan Bidder moi nap tien duoc"));
        }
    }

    private void handleGetAuctionHistory() {
        List<AuctionSnapshot> finished = auctionManager.getAllAuctions().stream()
            .filter(a -> a.getStatus() == Auction.Status.FINISHED
                      || a.getStatus() == Auction.Status.PAID)
            .map(AuctionSnapshot::from)
            .sorted((a, b) -> b.getEndTime().compareTo(a.getEndTime()))
            .collect(Collectors.toList());
        sendMessage(new Message(Message.Type.AUCTION_HISTORY_RESPONSE, finished));
    }

    private void handleChatMessage(Message message) {
        ChatMessage chat = (ChatMessage) message.getPayload();

        // Validate: not empty, not too long
        if (chat.getContent() == null || chat.getContent().isBlank()) return;
        if (chat.getContent().length() > 200) return;

        // Broadcast to everyone in the same auction room
        Message broadcast = new Message(Message.Type.CHAT_MESSAGE, chat);
        AuctionServer.broadcastToRoom(chat.getAuctionId(), broadcast);
    }

    private void handleSubmitReview(Message message) {
        ReviewRequest req = (ReviewRequest) message.getPayload();

        // Verify: reviewer must have WON that auction
        Auction auction = AuctionServer.getAuctionManager().findById(req.getAuctionId());
        if (auction == null || !req.getReviewerId().equals(auction.getWinnerId())) {
            sendMessage(new Message(Message.Type.REVIEW_RESPONSE,
                null, null, 0, "Chỉ người thắng cuộc mới có thể đánh giá"));
            return;
        }

        // Validate rating
        if (req.getRating() < 1 || req.getRating() > 5) {
            sendMessage(new Message(Message.Type.REVIEW_RESPONSE,
                null, null, 0, "Đánh giá phải từ 1–5 sao"));
            return;
        }

        User seller = UserManager.getInstance().findById(req.getSellerId());
        if (seller instanceof com.bidplaza.model.user.Seller s) {
            com.bidplaza.model.Review review = new com.bidplaza.model.Review(
                req.getReviewerId(),
                UserManager.getInstance().findById(req.getReviewerId()).getUsername(),
                req.getRating(),
                req.getComment(),
                req.getAuctionId()
            );
            s.addReview(review);
            saveData();
            sendMessage(new Message(Message.Type.REVIEW_RESPONSE,
                null, null, s.getAverageRating(), "Đánh giá thành công!"));
        }
    }

    // ── Utilities ─────────────────────────────────────────────────

    private void handleGetSystemStats() {
        List<User> users = userManager.getAllUsers();
        long bidders = users.stream().filter(u -> u instanceof Bidder).count();
        long sellers = users.stream().filter(u -> u instanceof Seller).count();
        long running = auctionManager.getAllAuctions().stream()
            .filter(a -> a.getStatus() == Auction.Status.RUNNING).count();
        long finished = auctionManager.getAllAuctions().stream()
            .filter(a -> a.getStatus() == Auction.Status.FINISHED
                      || a.getStatus() == Auction.Status.PAID).count();
        double total = auctionManager.getAllAuctions().stream()
            .filter(a -> a.getStatus() == Auction.Status.FINISHED
                      || a.getStatus() == Auction.Status.PAID)
            .mapToDouble(a -> a.getItem().getCurrentPrice())
            .sum();

        sendMessage(new Message(Message.Type.SYSTEM_STATS_RESPONSE,
            new SystemStats(users.size(), (int) bidders, (int) sellers,
                (int) running, (int) finished, total)));
    }

    private void handleGetAllUsers() {
        List<UserInfo> result = new ArrayList<>();
        for (User user : userManager.getAllUsers()) {
            double balance = user instanceof Bidder bidder ? bidder.getBalance() : 0;
            result.add(new UserInfo(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getEmail(),
                user.isBanned(),
                balance
            ));
        }
        sendMessage(new Message(Message.Type.ALL_USERS_RESPONSE, result));
    }

    private void handleBanUser(Message message) {
        String userId = message.getBidderId();
        User user = userManager.findById(userId);
        if (user == null) {
            sendMessage(Message.error("User not found"));
            return;
        }

        user.setBanned(true);
        saveData();
        sendMessage(new Message(Message.Type.BAN_USER_RESPONSE,
            null, userId, 0, "User banned"));
    }

    private void handleForceClose(Message message) {
        Auction auction = auctionManager.findById(message.getAuctionId());
        if (auction == null) {
            sendMessage(Message.error("Khong tim thay phien: " + message.getAuctionId()));
            return;
        }
        if (auction.getStatus() != Auction.Status.RUNNING) {
            sendMessage(Message.error("Chi co the dong phien RUNNING"));
            return;
        }

        auction.finish();
        saveData();
        notifyAuctionWinner(auction);
        AuctionSnapshot snapshot = AuctionSnapshot.from(auction);
        AuctionServer.broadcast(new Message(Message.Type.AUCTION_UPDATE, snapshot), this);
        sendMessage(new Message(Message.Type.ADMIN_FORCE_CLOSE, snapshot));
    }

    private void handleGetProfile(Message message) {
        String userId = message.getBidderId();
        User user = userManager.findById(userId);
        if (user == null) {
            sendMessage(Message.error("User not found"));
            return;
        }

        int totalBids = 0;
        int totalWon = 0;
        int totalCreated = 0;
        int totalSold = 0;
        double balance = 0;

        if (user instanceof Bidder bidder) {
            balance = bidder.getBalance();
            for (Auction auction : auctionManager.getAllAuctions()) {
                totalBids += (int) auction.getBids().stream()
                    .filter(tx -> tx.getBidderId().equals(userId)).count();
                if (userId.equals(auction.getWinnerId())) {
                    totalWon++;
                }
            }
        } else if (user instanceof Seller) {
            for (Auction auction : auctionManager.getAllAuctions()) {
                String sellerId = auction.getItem().getSellerId();
                if (userId.equals(sellerId) || user.getUsername().equals(sellerId)) {
                    totalCreated++;
                    if (auction.getStatus() == Auction.Status.FINISHED
                            || auction.getStatus() == Auction.Status.PAID) {
                        totalSold++;
                    }
                }
            }
        }

        ProfileData profile = new ProfileData(
            user.getUsername(), user.getEmail(), user.getRole(),
            balance, totalBids, totalWon, totalCreated, totalSold);
        sendMessage(new Message(Message.Type.PROFILE_RESPONSE, profile));
    }

    private void handleUpdatePassword(Message message) {
        String[] parts = message.getInfo() != null
            ? message.getInfo().split(":", 2)
            : new String[0];
        String userId = message.getBidderId();
        User user = userManager.findById(userId);

        if (parts.length == 2 && user != null && user.checkPassword(parts[0])) {
            user.setPassword(parts[1]);
            saveData();
            sendMessage(new Message(Message.Type.UPDATE_PASSWORD_RESPONSE,
                null, userId, 0, "SUCCESS"));
        } else {
            sendMessage(new Message(Message.Type.UPDATE_PASSWORD_RESPONSE,
                null, userId, 0, "WRONG_PASSWORD"));
        }
    }

    private void handleGetNotifications(Message message) {
        User user = userManager.findById(message.getBidderId());
        if (user == null) {
            sendMessage(Message.error("User not found"));
            return;
        }
        sendMessage(new Message(Message.Type.NOTIFICATIONS_RESPONSE,
            new ArrayList<>(user.getNotifications())));
    }

    private void handleMarkNotificationsRead(Message message) {
        User user = userManager.findById(message.getBidderId());
        if (user == null) {
            sendMessage(Message.error("User not found"));
            return;
        }
        user.getNotifications().forEach(n -> n.setRead(true));
        saveData();
        sendMessage(new Message(Message.Type.NOTIFICATIONS_RESPONSE,
            new ArrayList<>(user.getNotifications())));
    }

    private void notifyAuctionWinner(Auction auction) {
        if (auction.getWinnerId() == null) {
            return;
        }
        notifyUser(auction.getWinnerId(), new Notification(
            "Ban thang phien dau gia",
            "Ban da thang phien: " + auction.getItem().getName(),
            "WON"));
    }

    private void notifyUser(String userId, Notification notification) {
        User user = userManager.findById(userId);
        if (user != null) {
            user.addNotification(notification);
            saveData();
        }
        AuctionServer.pushNotification(userId, notification);
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
        if (currentAuctionId != null) {
            AuctionServer.leaveRoom(currentAuctionId, this);
        }
        if (currentUserId != null) {
            AuctionServer.unregisterClient(currentUserId);
        }
        try {
            if (in  != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }

    private void saveData() {
        com.bidplaza.storage.DataStorage.save(
                auctionManager, UserManager.getInstance());
    }
}
