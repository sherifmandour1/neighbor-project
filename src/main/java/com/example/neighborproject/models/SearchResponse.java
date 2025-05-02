package com.example.neighborproject.models;

import java.util.List;

public class SearchResponse {
    private String locationId;
    private List<String> listingIds;
    private int totalPriceInCents;

    public SearchResponse() {}

    public SearchResponse(String locationId, List<String> listingIds, int totalPriceInCents) {
        this.locationId = locationId;
        this.listingIds = listingIds;
        this.totalPriceInCents = totalPriceInCents;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public List<String> getListingIds() {
        return listingIds;
    }

    public void setListingIds(List<String> listingIds) {
        this.listingIds = listingIds;
    }

    public int getTotalPriceInCents() {
        return totalPriceInCents;
    }

    public void setTotalPriceInCents(int totalPriceInCents) {
        this.totalPriceInCents = totalPriceInCents;
    }
}