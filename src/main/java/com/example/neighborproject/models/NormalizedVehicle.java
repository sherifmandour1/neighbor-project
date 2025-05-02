package com.example.neighborproject.models;

/**
 * Represents a normalized vehicle with dimensions in grid units.
 * Used for internal processing of vehicle placement algorithms.
 */
public class NormalizedVehicle {
    private final int length;
    private final int width;
    private final int quantity;
    private final int area;

    public NormalizedVehicle(int length, int width, int quantity, int area) {
        this.length = length;
        this.width = width;
        this.quantity = quantity;
        this.area = area;
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

    public int getArea() {
        return area;
    }
}