package org.example.turism_app.dto;

import org.example.turism_app.model.Destination;

import java.time.LocalDate;

public record DestinationResponse(
        Long id,
        String city,
        String country,
        LocalDate arrivalDate,
        LocalDate departureDate,
        Long tripId
) {
    public static DestinationResponse from(Destination d) {
        return new DestinationResponse(
                d.getId(),
                d.getCity(),
                d.getCountry(),
                d.getArrivalDate(),
                d.getDepartureDate(),
                d.getTrip().getId()
        );
    }
}
