package com.example.neighborproject.controllers;

import com.example.neighborproject.models.*;
import com.example.neighborproject.services.ListingService;
import com.example.neighborproject.services.VehiclePlacementService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Controller handling vehicle space search and allocation requests.
 * Uses optimized bin packing algorithms to find the most cost-effective
 * solutions for storing vehicles in available spaces.
 */
@RestController
@RequestMapping("/search")
public class VehicleSearchController {

    private static final Logger log = LoggerFactory.getLogger(VehicleSearchController.class);

    @Autowired
    private ListingService listingService;

    @Autowired
    private VehiclePlacementService placementService;

    private final ExecutorService executorService = Executors.newWorkStealingPool();

    /**
     * Handles search requests for optimal vehicle placements.
     *
     * @param vehicles List of vehicles with their dimensions and quantities
     * @return List of search responses sorted by price
     */
    @PostMapping("/spaces")
    public ResponseEntity<?> search(@RequestBody List<VehicleRequest> vehicles) {
        try {
            if (vehicles == null || vehicles.isEmpty()) {
                return ResponseEntity.badRequest().body("No vehicles provided in request");
            }

            // Normalize all vehicle dimensions
            List<NormalizedVehicle> normalizedVehicles = normalizeVehicles(vehicles);

            // Get all available locations
            Map<String, List<Listing>> locationListings = listingService.getListingsByLocation();

            // Process each location in parallel for better performance
            List<CompletableFuture<SearchResponse>> futures = new ArrayList<>();

            for (Map.Entry<String, List<Listing>> entry : locationListings.entrySet()) {
                String locationId = entry.getKey();
                List<Listing> locationSpaces = entry.getValue();

                CompletableFuture<SearchResponse> future = CompletableFuture.supplyAsync(() ->
                                findOptimalSolution(locationId, locationSpaces, normalizedVehicles),
                        executorService
                );

                futures.add(future);
            }

            List<SearchResponse> results = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingInt(SearchResponse::getTotalPriceInCents))
                    .collect(Collectors.toList());

            if (results.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error processing search request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing search request: " + e.getMessage());
        }
    }


    private List<NormalizedVehicle> normalizeVehicles(List<VehicleRequest> vehicles) {
        List<NormalizedVehicle> result = new ArrayList<>();

        for (VehicleRequest req : vehicles) {
            // Convert to normalized grid units
            int normalizedLength = req.getLength() / 10;
            int normalizedWidth = Math.max(1, 1/ 10);

            result.add(new NormalizedVehicle(
                    normalizedLength,
                    normalizedWidth,
                    req.getQuantity(),
                    normalizedLength * normalizedWidth
            ));
        }

        result.sort(Comparator.comparingInt(NormalizedVehicle::getArea).reversed());
        return result;
    }

    /**
     * Find the optimal solution for a given location using multiple strategies.
     */
    private SearchResponse findOptimalSolution(String locationId,
                                               List<Listing> availableListings,
                                               List<NormalizedVehicle> vehicles) {
        try {
            SearchResponse singleSpaceSolution = trySingleSpaceSolution(locationId, availableListings, vehicles);
            if (singleSpaceSolution != null) {
                return singleSpaceSolution;
            }

            SearchResponse multiSpaceSolution = tryMultipleSpaceSolution(locationId, availableListings, vehicles);
            if (multiSpaceSolution != null) {
                return multiSpaceSolution;
            }

            return null;
        } catch (Exception e) {
            log.error("Error finding solution for location {}", locationId, e);
            return null;
        }
    }

    /**
     * Attempt to fit all vehicles into a single space (optimal for cost).
     */
    private SearchResponse trySingleSpaceSolution(String locationId,
                                                  List<Listing> listings,
                                                  List<NormalizedVehicle> vehicles) {
        List<Listing> sortedListings = new ArrayList<>(listings);
        sortedListings.sort(Comparator.comparingInt(Listing::getPriceInCents));

        for (Listing listing : sortedListings) {
            if (placementService.canFitAllVehicles(listing, vehicles)) {
                return new SearchResponse(
                        locationId,
                        Collections.singletonList(listing.getId()),
                        listing.getPriceInCents()
                );
            }
        }

        return null;
    }

    /**
     * Attempt to distribute vehicles across multiple spaces when a single space won't work.
     * Uses more advanced bin packing algorithms.
     */
    private SearchResponse tryMultipleSpaceSolution(String locationId,
                                                    List<Listing> listings,
                                                    List<NormalizedVehicle> vehiclesList) {
        List<NormalizedVehicle> individualVehicles = expandVehicleList(vehiclesList);

        List<Listing> sortedListings = new ArrayList<>(listings);
        sortedListings.sort(Comparator.comparingDouble(
                l -> (double) l.getPriceInCents() / (l.getLength() * l.getWidth() / 100)
        ));

        OptimalPackingResult result = placementService.findOptimalPacking(
                sortedListings, individualVehicles);

        if (result != null && !result.getUsedListingIds().isEmpty()) {
            return new SearchResponse(
                    locationId,
                    result.getUsedListingIds(),
                    result.getTotalPrice()
            );
        }

        return null;
    }

    /**
     * Expand vehicle list to individual units for multi-space allocation.
     */
    private List<NormalizedVehicle> expandVehicleList(List<NormalizedVehicle> vehicles) {
        List<NormalizedVehicle> expanded = new ArrayList<>();

        for (NormalizedVehicle vehicle : vehicles) {
            for (int i = 0; i < vehicle.getQuantity(); i++) {
                expanded.add(new NormalizedVehicle(
                        vehicle.getLength(),
                        vehicle.getWidth(),
                        1,
                        vehicle.getArea()
                ));
            }
        }

        expanded.sort(Comparator.comparingInt(NormalizedVehicle::getArea).reversed());
        return expanded;
    }
}