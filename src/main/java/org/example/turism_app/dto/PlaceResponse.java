package org.example.turism_app.dto;

import org.example.turism_app.model.Place;
import org.example.turism_app.model.PlaceCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PlaceResponse(
        Long id,
        String name,
        PlaceCategory category,
        LocalDate plannedDate,
        BigDecimal cost,
        boolean visited,
        Integer rating,
        Long destinationId
) {
    public static PlaceResponse from(Place p) {
        return new PlaceResponse(
                p.getId(),
                p.getName(),
                p.getCategory(),
                p.getPlannedDate(),
                p.getCost(),
                p.isVisited(),
                p.getRating(),
                p.getDestination().getId()
        );
    }
}
