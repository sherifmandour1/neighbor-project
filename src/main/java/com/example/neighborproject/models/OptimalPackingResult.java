package com.example.neighborproject.models;

import java.util.List;

/**
 * Result of an optimal packing operation.
 */
public class OptimalPackingResult {
    private final List<String> usedListingIds;
    private final int totalPrice;

    public OptimalPackingResult(List<String> usedListingIds, int totalPrice) {
        this.usedListingIds = usedListingIds;
        this.totalPrice = totalPrice;
    }

    public List<String> getUsedListingIds() {
        return usedListingIds;
    }

    public int getTotalPrice() {
        return totalPrice;
    }
}