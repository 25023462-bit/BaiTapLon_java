package com.bidplaza;

import com.bidplaza.factory.ItemFactory;
import com.bidplaza.model.Auction;
import com.bidplaza.model.item.Item;
import com.bidplaza.model.user.Bidder;
import com.bidplaza.network.AuctionSnapshot;
import com.bidplaza.network.AutoBidRequest;
import com.bidplaza.network.BidHistoryResponse;
import com.bidplaza.network.BidTransactionInfo;
import com.bidplaza.network.ChatMessage;
import com.bidplaza.network.CreateAuctionRequest;
import com.bidplaza.network.DepositRequest;
import com.bidplaza.network.LoginRequest;
import com.bidplaza.network.LoginResponse;
import com.bidplaza.network.Message;
import com.bidplaza.network.ReviewRequest;
import com.bidplaza.network.ServerPort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkDtoTest {

    @AfterEach
    void clearPortOverride() {
        System.clearProperty("bidplaza.port");
    }

    @Test
    void requestPayloads_exposeConstructorValues() {
        CreateAuctionRequest create = new CreateAuctionRequest(
            "Laptop", "desc", "electronics", 100.0, 2, "seller-1");
        assertEquals("Laptop", create.getName());
        assertEquals("desc", create.getDescription());
        assertEquals("electronics", create.getCategory());
        assertEquals(100.0, create.getStartingPrice());
        assertEquals(2, create.getDurationHours());
        assertEquals("seller-1", create.getSellerId());

        DepositRequest deposit = new DepositRequest("user-1", 250.0);
        assertEquals("user-1", deposit.getUserId());
        assertEquals(250.0, deposit.getAmount());

        AutoBidRequest autoBid = new AutoBidRequest("auction-1", "bidder-1", 500.0, 25.0);
        assertEquals("auction-1", autoBid.getAuctionId());
        assertEquals("bidder-1", autoBid.getBidderId());
        assertEquals(500.0, autoBid.getMaxBid());
        assertEquals(25.0, autoBid.getIncrement());
    }

    @Test
    void chatReviewAndBidDtos_exposeValues() {
        ChatMessage chat = new ChatMessage("auction-1", "sender-1", "Alice", "hello");
        assertEquals("auction-1", chat.getAuctionId());
        assertEquals("sender-1", chat.getSenderId());
        assertEquals("Alice", chat.getSenderUsername());
        assertEquals("hello", chat.getContent());
        assertNotNull(chat.getTimestamp());

        ReviewRequest review = new ReviewRequest("reviewer-1", "seller-1", "auction-1", 4.5, "good");
        assertEquals("reviewer-1", review.getReviewerId());
        assertEquals("seller-1", review.getSellerId());
        assertEquals("auction-1", review.getAuctionId());
        assertEquals(4.5, review.getRating());
        assertEquals("good", review.getComment());

        LocalDateTime timestamp = LocalDateTime.now();
        BidTransactionInfo bid = new BidTransactionInfo(
            "auction-1", "Laptop", 123.0, timestamp, "ACTIVE");
        assertEquals("auction-1", bid.getAuctionId());
        assertEquals("Laptop", bid.getAuctionName());
        assertEquals(123.0, bid.getAmount());
        assertEquals(timestamp, bid.getTimestamp());
        assertEquals("ACTIVE", bid.getStatus());
    }

    @Test
    void bidHistoryResponse_copiesAndProtectsList() {
        BidTransactionInfo bid = new BidTransactionInfo(
            "auction-1", "Laptop", 123.0, LocalDateTime.now(), "ACTIVE");
        BidHistoryResponse response = new BidHistoryResponse(List.of(bid));

        assertEquals(1, response.getBids().size());
        assertThrows(UnsupportedOperationException.class,
            () -> response.getBids().add(bid));
        assertTrue(new BidHistoryResponse(null).getBids().isEmpty());
    }

    @Test
    void loginPayloadsAndResponses_exposeValues() {
        LoginRequest request = new LoginRequest("alice", "secret", "BIDDER", true);
        assertEquals("alice", request.getUsername());
        assertEquals("secret", request.getPassword());
        assertEquals("BIDDER", request.getRole());
        assertTrue(request.isRegister());

        Bidder user = new Bidder("alice", "secret", "alice@test.local");
        LoginResponse response = new LoginResponse(true, "ok", user);
        assertTrue(response.isSuccess());
        assertEquals("ok", response.getMessage());
        assertEquals(user, response.getUser());
    }

    @Test
    void messageFactories_setExpectedFieldsAndSuccessFlags() {
        Message bid = Message.placeBid("auction-1", "bidder-1", 150.0);
        assertEquals(Message.Type.PLACE_BID, bid.getType());
        assertEquals("auction-1", bid.getAuctionId());
        assertEquals("bidder-1", bid.getBidderId());
        assertEquals(150.0, bid.getAmount());
        assertTrue(bid.isSuccess());

        Message success = Message.bidSuccess("auction-1", 200.0);
        assertEquals(Message.Type.BID_SUCCESS, success.getType());
        assertEquals(200.0, success.getAmount());
        assertEquals("Bid thanh cong!", success.getMessage());

        Message failed = Message.bidFailed("auction-1", "low bid");
        assertFalse(failed.isSuccess());
        assertEquals("low bid", failed.getInfo());

        Message error = Message.error("bad request");
        assertFalse(error.isSuccess());
        assertEquals("bad request", error.getMessage());
        assertTrue(error.toString().contains("ERROR"));
    }

    @Test
    void messageLoginAndPayloadHelpers_returnExpectedData() {
        Message login = Message.login("alice", "secret", "BIDDER", false);
        assertEquals(Message.Type.LOGIN, login.getType());
        assertInstanceOf(LoginRequest.class, login.getPayload());
        assertEquals(login.getPayload(), login.getData());

        Bidder user = new Bidder("alice", "secret", "alice@test.local");
        Message loginOk = Message.loginResponse(true, "ok", user);
        assertTrue(loginOk.isSuccess());
        assertEquals(user, loginOk.getData());

        Message loginFailed = Message.loginResponse(false, "no");
        assertFalse(loginFailed.isSuccess());
        assertNull(loginFailed.getData());

        Message depositOk = new Message(Message.Type.DEPOSIT_SUCCESS, null, null, 99.0, "ok");
        assertEquals(99.0, depositOk.getData());

        Message autoFailed = new Message(Message.Type.AUTO_BID_FAILED, null, null, 0, "bad");
        assertFalse(autoFailed.isSuccess());

        Message depositFailed = new Message(Message.Type.DEPOSIT_FAILED, null, null, 0, "bad");
        assertFalse(depositFailed.isSuccess());
    }

    @Test
    void messageAuctionUpdateAndAutoBid_includePayloads() {
        AuctionSnapshot snapshot = new AuctionSnapshot(
            "auction-1", "Laptop", "desc", "ELECTRONICS",
            100.0, 150.0, "RUNNING", LocalDateTime.now(),
            LocalDateTime.now().plusHours(1), "seller-1", "winner-1",
            "winner", 1, Collections.emptyList());
        Message update = Message.auctionUpdate("auction-1", 150.0, "winner-1", snapshot);
        assertEquals(Message.Type.AUCTION_UPDATE, update.getType());
        assertEquals("winner-1", update.getBidderId());
        assertEquals(snapshot, update.getPayload());

        Message updateWithoutSnapshot = Message.auctionUpdate("auction-1", 151.0, "winner-1");
        assertNull(updateWithoutSnapshot.getPayload());

        Message autoBid = Message.registerAutoBid("auction-1", "bidder-1", 500.0, 25.0);
        assertEquals(Message.Type.REGISTER_AUTO_BID, autoBid.getType());
        assertInstanceOf(AutoBidRequest.class, autoBid.getPayload());
    }

    @Test
    void auctionSnapshot_directAndFromAuction_exposeValues() throws Exception {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(1);
        BidTransactionInfo history = new BidTransactionInfo(
            "auction-1", "Laptop", 150.0, start, "ACTIVE");
        AuctionSnapshot direct = new AuctionSnapshot(
            "auction-1", "Laptop", "desc", "ELECTRONICS",
            100.0, 150.0, "RUNNING", start, end, "seller-1",
            "winner-1", "winner", 1, List.of(history));

        assertEquals("auction-1", direct.getId());
        assertEquals("Laptop", direct.getName());
        assertEquals("desc", direct.getDescription());
        assertEquals("ELECTRONICS", direct.getCategory());
        assertEquals(100.0, direct.getStartingPrice());
        assertEquals(150.0, direct.getCurrentPrice());
        assertEquals("RUNNING", direct.getStatus());
        assertEquals(start, direct.getStartTime());
        assertEquals(end, direct.getEndTime());
        assertEquals("seller-1", direct.getSellerId());
        assertEquals("winner-1", direct.getWinnerId());
        assertEquals("winner", direct.getWinnerUsername());
        assertEquals(1, direct.getBidCount());
        assertThrows(UnsupportedOperationException.class,
            () -> direct.getBidHistory().add(history));

        Item item = ItemFactory.create(
            "electronics", "Phone", "desc", 100.0, start, end, "seller-1");
        Auction auction = new Auction(item);
        auction.start();
        auction.placeBid("bidder-1", 120.0);

        AuctionSnapshot fromAuction = AuctionSnapshot.from(auction);
        assertEquals(auction.getId(), fromAuction.getId());
        assertEquals("Phone", fromAuction.getName());
        assertEquals(120.0, fromAuction.getCurrentPrice());
        assertEquals("RUNNING", fromAuction.getStatus());
        assertEquals("bidder-1", fromAuction.getWinnerId());
        assertEquals(1, fromAuction.getBidCount());
        assertEquals(1, fromAuction.getBidHistory().size());
    }

    @Test
    void serverPort_usesDefaultOverrideAndFallback() {
        assertEquals(ServerPort.DEFAULT, ServerPort.get());

        System.setProperty("bidplaza.port", "19090");
        assertEquals(19090, ServerPort.get());

        System.setProperty("bidplaza.port", "not-a-number");
        assertEquals(ServerPort.DEFAULT, ServerPort.get());
    }
}
