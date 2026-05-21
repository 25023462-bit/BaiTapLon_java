package com.bidplaza.network;

import com.bidplaza.manager.AuctionManager;
import com.bidplaza.model.Auction;
import com.bidplaza.storage.DataStorage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Thread-safe Timer task running periodically to detect and finish expired auctions.
 */
public class AuctionTimer implements Runnable {

    private final AuctionManager auctionManager;
    private final DataStorage dataStorage;

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
                if (auction.getItem().getEndTime().isBefore(LocalDateTime.now())) {
                    synchronized (auction) {
                        // Double check status to avoid race conditions
                        if (auction.getStatus() == Auction.Status.RUNNING) {
                            auction.finish();

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
}
