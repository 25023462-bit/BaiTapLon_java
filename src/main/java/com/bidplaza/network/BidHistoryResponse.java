package com.bidplaza.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BidHistoryResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<BidTransactionInfo> bids;

    public BidHistoryResponse(List<BidTransactionInfo> bids) {
        this.bids = bids != null ? new ArrayList<>(bids) : new ArrayList<>();
    }

    public List<BidTransactionInfo> getBids() {
        return Collections.unmodifiableList(bids);
    }
}
