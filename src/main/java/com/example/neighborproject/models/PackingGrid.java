package com.example.neighborproject.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a grid for spatial allocation of vehicles.
 * Used for determining if vehicles can be placed in a space.
 */
public class PackingGrid {
    private final int length;
    private final int width;
    private final boolean[][] occupied;

    public PackingGrid(int length, int width) {
        this.length = length;
        this.width = width;
        this.occupied = new boolean[length][width];
    }

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    /**
     * Checks if an area is available for placement
     */
    public boolean canPlace(int x, int y, int itemLength, int itemWidth) {
        if (x < 0 || y < 0 || x + itemLength > length || y + itemWidth > width) {
            return false;
        }

        for (int i = x; i < x + itemLength; i++) {
            for (int j = y; j < y + itemWidth; j++) {
                if (occupied[i][j]) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Marks an area as occupied
     */
    public void place(int x, int y, int itemLength, int itemWidth) {
        for (int i = x; i < x + itemLength; i++) {
            for (int j = y; j < y + itemWidth; j++) {
                occupied[i][j] = true;
            }
        }
    }

    /**
     * Calculates the current utilization percentage of the grid
     */
    @JsonIgnore
    public double getUtilizationPercentage() {
        int totalCells = length * width;
        int occupiedCells = 0;

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < width; j++) {
                if (occupied[i][j]) {
                    occupiedCells++;
                }
            }
        }

        return (double) occupiedCells / totalCells;
    }
}