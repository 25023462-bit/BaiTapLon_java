package com.bidplaza.network;

import com.bidplaza.manager.AuctionManager;
import com.bidplaza.manager.UserManager;
import com.bidplaza.model.Auction;
import com.bidplaza.model.Notification;
import com.bidplaza.model.user.Bidder;
import com.bidplaza.model.user.User;
import com.bidplaza.storage.DataStorage;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe Timer task running periodically to detect and finish expired auctions.
 */
public class AuctionTimer implements Runnable {

    private final AuctionManager auctionManager;
    private final DataStorage dataStorage;
    private static final Set<String> endingSoonSent =
        ConcurrentHashMap.newKeySet();

    public AuctionTimer(AuctionManager auctionManager, DataStorage dataStorage) {
        this.auctionManager = auctionManager;
        this.dataStorage = dataStorage;
    }

    @Override
    public void run() {
        try {
            List<Auction> runningAuctions = new ArrayList<>();
            // Safely snapshot running auctions to prevent ConcurrentModificationException
            synchronized (auctionManager) {
                for (Auction auction : auctionManager.getAllAuctions()) {
                    if (auction.getStatus() == Auction.Status.RUNNING) {
                        runningAuctions.add(auction);
                    }
                }
            }

            for (Auction auction : runningAuctions) {
                maybeSendEndingSoonNotifications(auction);
                if (auction.getItem().getEndTime().isBefore(LocalDateTime.now())) {
                    synchronized (auction) {
                        // Double check status to avoid race conditions
                        if (auction.getStatus() == Auction.Status.RUNNING) {
                            auction.finish();
                            notifyWinner(auction);

                            // Thread-safe persistence
                            synchronized (auctionManager) {
                                DataStorage.save(auctionManager);
                            }

                            // Broadcast update to all clients
                            Message update = new Message(
                                Message.Type.AUCTION_UPDATE,
                                AuctionSnapshot.from(auction)
                            );
                            AuctionServer.broadcast(update, null);

                            // Thread-safe clear logging
                            System.out.println("[AuctionTimer] Phiên đấu giá kết thúc thành công: " + auction.getId()
                                    + " | Sản phẩm: " + auction.getItem().getName()
                                    + " | Người thắng: " + (auction.getWinnerId() != null ? auction.getWinnerId() : "Không có")
                                    + " | Giá cuối: $" + auction.getItem().getCurrentPrice());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[AuctionTimer] Lỗi trong quá trình kiểm tra phiên đấu giá: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void maybeSendEndingSoonNotifications(Auction auction) {
        long secondsLeft = LocalDateTime.now()
            .until(auction.getItem().getEndTime(), ChronoUnit.SECONDS);
        if (secondsLeft <= 0 || secondsLeft > 300) {
            return;
        }

        for (User user : UserManager.getInstance().getAllUsers()) {
            if (user instanceof Bidder bidder && bidder.isWatching(auction.getId())) {
                String key = user.getId() + ":" + auction.getId();
                if (endingSoonSent.add(key)) {
                    Notification notification = new Notification(
                        "Phien sap ket thuc",
                        "Phien " + auction.getItem().getName()
                            + " sap ket thuc trong 5 phut",
                        "ENDING_SOON");
                    user.addNotification(notification);
                    AuctionServer.pushNotification(user.getId(), notification);
                }
            }
        }
    }

    private void notifyWinner(Auction auction) {
        if (auction.getWinnerId() == null) {
            return;
        }
        User winner = UserManager.getInstance().findById(auction.getWinnerId());
        Notification notification = new Notification(
            "Ban thang phien dau gia",
            "Ban da thang phien: " + auction.getItem().getName(),
            "WON");
        if (winner != null) {
            winner.addNotification(notification);
        }
        AuctionServer.pushNotification(auction.getWinnerId(), notification);
    }
}
