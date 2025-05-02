package com.example.neighborproject.services;

import com.example.neighborproject.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service responsible for vehicle placement algorithms and bin packing strategies.
 * Implements various optimization algorithms to efficiently place vehicles in spaces.
 */
@Service
public class VehiclePlacementService {
    private static final Logger log = LoggerFactory.getLogger(VehiclePlacementService.class);


    public boolean canFitAllVehicles(Listing listing, List<NormalizedVehicle> vehicles) {
        // Create a grid representation of the space
        int gridLength = listing.getLength() / 10;
        int gridWidth = listing.getWidth() / 10;

        PackingGrid grid = new PackingGrid(gridLength, gridWidth);

        // Try to place each vehicle
        for (NormalizedVehicle vehicle : vehicles) {
            for (int i = 0; i < vehicle.getQuantity(); i++) {
                boolean placed = tryPlaceVehicleWithRotation(grid, vehicle);
                if (!placed) {
                    return false; // Failed to place one vehicle - solution not feasible
                }
            }
        }

        return true; // All vehicles successfully placed
    }


    private boolean tryPlaceVehicleWithRotation(PackingGrid grid, NormalizedVehicle vehicle) {
        int vLength = vehicle.getLength();
        int vWidth = vehicle.getWidth();

        // Try original orientation (using bottom-left strategy)
        if (tryBottomLeftPlacement(grid, vLength, vWidth)) {
            return true;
        }

        // Try rotated orientation (if different dimensions)
        if (vLength != vWidth && tryBottomLeftPlacement(grid, vWidth, vLength)) {
            return true;
        }

        // Cannot place the vehicle in any orientation
        return false;
    }


    private boolean tryBottomLeftPlacement(PackingGrid grid, int itemLength, int itemWidth) {
        int gridLength = grid.getLength();
        int gridWidth = grid.getWidth();

        // Create a list of potential positions (x, y) sorted by bottom-left preference
        List<Location> locations = new ArrayList<>();

        for (int y = 0; y <= gridWidth - itemWidth; y++) {
            for (int x = 0; x <= gridLength - itemLength; x++) {
                locations.add(new Location(x, y));
            }
        }

        // Sort positions by y first (bottom), then by x (left)
        locations.sort(Comparator.comparingInt(Location::getY)
                .thenComparingInt(Location::getX));

        // Try each position in the sorted order
        for (Location pos : locations) {
            int x = pos.getX();
            int y = pos.getY();

            if (grid.canPlace(x, y, itemLength, itemWidth)) {
                grid.place(x, y, itemLength, itemWidth);
                return true;
            }
        }

        return false;
    }


    public OptimalPackingResult findOptimalPacking(List<Listing> listings,
                                                   List<NormalizedVehicle> vehicles) {
        Map<String, PackingGrid> usedListings = new LinkedHashMap<>();
        int totalPrice = 0;
        List<String> usedListingIds = new ArrayList<>();

        for (NormalizedVehicle vehicle : vehicles) {
            // First try to place in existing spaces
            boolean placed = false;

            // then try existing used spaces first
            for (Map.Entry<String, PackingGrid> entry : usedListings.entrySet()) {
                PackingGrid grid = entry.getValue();

                if (tryPlaceVehicleWithRotation(grid, vehicle)) {
                    placed = true;
                    break;
                }
            }

            // If not placed, try new space
            if (!placed) {
                // Try each available listing
                for (Listing listing : listings) {
                    // Skip if already using this listing
                    if (usedListings.containsKey(listing.getId())) {
                        continue;
                    }

                    int gridLength = listing.getLength() / 10;
                    int gridWidth = listing.getWidth() / 10;
                    PackingGrid grid = new PackingGrid(gridLength, gridWidth);

                    // Try to place the vehicle in this new space
                    if (tryPlaceVehicleWithRotation(grid, vehicle)) {
                        usedListings.put(listing.getId(), grid);
                        usedListingIds.add(listing.getId());
                        totalPrice += listing.getPriceInCents();
                        placed = true;
                        break;
                    }
                }
            }

            if (!placed) {
                return null;
            }
        }

        if (usedListings.isEmpty()) {
            return null;
        }

        return new OptimalPackingResult(usedListingIds, totalPrice);
    }
}