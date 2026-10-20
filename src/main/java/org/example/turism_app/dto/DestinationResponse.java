package org.example.turism_app.dto;

import org.example.turism_app.model.Destination;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DestinationResponse(
        Long id,
        String city,
        String country,
        LocalDate arrivalDate,
        LocalDate departureDate,
        Long tripId,
        String createdBy,
        LocalDateTime createdAt,
        String updatedBy,
        LocalDateTime updatedAt
) {
    public static DestinationResponse from(Destination d) {
        return new DestinationResponse(
                d.getId(),
                d.getCity(),
                d.getCountry(),
                d.getArrivalDate(),
                d.getDepartureDate(),
                d.getTrip().getId(),
                d.getCreatedBy(),
                d.getCreatedAt(),
                d.getUpdatedBy(),
                d.getUpdatedAt()
        );
    }
}
