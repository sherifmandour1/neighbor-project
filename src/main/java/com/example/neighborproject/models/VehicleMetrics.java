package com.example.neighborproject.models;

public class VehicleMetrics {
    private final int length;
    private final int width;
    private final int quantity;

    public VehicleMetrics(int length, int width, int quantity) {
        this.length = length;
        this.width = width;
        this.quantity = quantity;
    }

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getGridArea() {
        return length * width;
    }
}