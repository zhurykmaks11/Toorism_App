package org.example.turism_app.dto;

import org.example.turism_app.model.Trip;
import org.example.turism_app.model.TripStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TripResponse(
        Long id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal budget,
        TripStatus status,
        LocalDateTime createdAt
) {
    public static TripResponse from(Trip trip) {
        return new TripResponse(
                trip.getId(),
                trip.getTitle(),
                trip.getDescription(),
                trip.getStartDate(),
                trip.getEndDate(),
                trip.getBudget(),
                trip.getStatus(),
                trip.getCreatedAt()
        );
    }
}
