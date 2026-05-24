package com.bidplaza.network;

import java.io.Serializable;

public class SystemStats implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int totalUsers;
    private final int totalBidders;
    private final int totalSellers;
    private final int runningAuctions;
    private final int finishedAuctions;
    private final double totalTransactionValue;

    public SystemStats(int totalUsers, int totalBidders, int totalSellers,
                       int runningAuctions, int finishedAuctions,
                       double totalTransactionValue) {
        this.totalUsers = totalUsers;
        this.totalBidders = totalBidders;
        this.totalSellers = totalSellers;
        this.runningAuctions = runningAuctions;
        this.finishedAuctions = finishedAuctions;
        this.totalTransactionValue = totalTransactionValue;
    }

    public int getTotalUsers() { return totalUsers; }
    public int getTotalBidders() { return totalBidders; }
    public int getTotalSellers() { return totalSellers; }
    public int getRunningAuctions() { return runningAuctions; }
    public int getFinishedAuctions() { return finishedAuctions; }
    public double getTotalTransactionValue() { return totalTransactionValue; }
}
