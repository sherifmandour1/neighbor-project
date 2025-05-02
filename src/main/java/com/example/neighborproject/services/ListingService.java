package com.example.neighborproject.services;

import com.example.neighborproject.models.Listing;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service responsible for managing space listings data.
 * Handles loading, caching, and providing access to listing information.
 */
@Service
public class ListingService {
    private static final Logger log = LoggerFactory.getLogger(ListingService.class);

    @Value("${listings.file.path:#{systemProperties['user.dir'] + '/listings.json'}}")
    private String listingsFilePath;

    private final Map<String, List<Listing>> listingsByLocation = new ConcurrentHashMap<>();
    private final Map<String, Listing> listingsById = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);


    @PostConstruct
    public void initialize() {
        loadListings();
    }

    public void loadListings() {
        try {
            File file = new File(listingsFilePath);
            log.info("Loading listings from {}", file.getAbsolutePath());

            List<Listing> listings = objectMapper.readValue(file, new TypeReference<List<Listing>>() {});

            listingsByLocation.clear();
            listingsById.clear();

            Map<String, List<Listing>> tempListingsByLocation = listings.stream()
                    .collect(Collectors.groupingBy(Listing::getLocationId));

            // Sort by price within each location and store in concurrent map
            for (Map.Entry<String, List<Listing>> entry : tempListingsByLocation.entrySet()) {
                List<Listing> sortedListings = entry.getValue().stream()
                        .sorted(Comparator.comparingInt(Listing::getPriceInCents))
                        .collect(Collectors.toList());

                listingsByLocation.put(entry.getKey(), sortedListings);
            }

            for (Listing listing : listings) {
                listingsById.put(listing.getId(), listing);
            }

            log.info("Successfully loaded {} listings across {} locations",
                    listings.size(), listingsByLocation.size());

        } catch (IOException e) {
            log.error("Failed to load listings: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to load listings data", e);
        }
    }


    @Cacheable("listingsByLocation")
    public Map<String, List<Listing>> getListingsByLocation() {
        return new HashMap<>(listingsByLocation);
    }


    @Cacheable("listingsById")
    public Optional<Listing> getListingById(String id) {
        return Optional.ofNullable(listingsById.get(id));
    }


    @Cacheable("listingsByLocationId")
    public List<Listing> getListingsByLocationId(String locationId) {
        return listingsByLocation.getOrDefault(locationId, Collections.emptyList());
    }
}