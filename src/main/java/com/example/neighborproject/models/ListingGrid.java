package com.example.neighborproject.models;

public class ListingGrid {
    private final int length;
    private final int width;
    private final boolean[][] grid;

    // Track occupied area for quick rejection
    // To act almost as a caching layer for O(1) Checking
    private int occupiedArea = 0;
    private final int totalArea;

    public ListingGrid(int length, int width) {
        this.length = length;
        this.width = width;
        this.grid = new boolean[length][width];
        this.totalArea = length * width;
    }

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    public boolean canPlace(int startL, int startW, int vehicleLength, int vehicleWidth) {
        int requiredArea = vehicleLength * vehicleWidth;
        if (occupiedArea + requiredArea > totalArea) {
            return false;
        }


        for (int l = startL; l < startL + vehicleLength; l++) {
            for (int w = startW; w < startW + vehicleWidth; w++) {
                if (grid[l][w]) {
                    return false;
                }
            }
        }

        return true;
    }

    public void place(int startL, int startW, int vehicleLength, int vehicleWidth) {
        for (int l = startL; l < startL + vehicleLength; l++) {
            for (int w = startW; w < startW + vehicleWidth; w++) {
                grid[l][w] = true;
            }
        }

        occupiedArea += vehicleLength * vehicleWidth;
    }
}