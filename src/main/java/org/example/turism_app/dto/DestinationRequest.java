package org.example.turism_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DestinationRequest(
        @NotBlank(message = "Місто обов'язкове")
        @Size(max = 100, message = "Назва міста не довша за 100 символів")
        String city,

        @NotBlank(message = "Країна обов'язкова")
        @Size(max = 100, message = "Назва країни не довша за 100 символів")
        String country,

        @NotNull(message = "Дата прибуття обов'язкова")
        LocalDate arrivalDate,

        @NotNull(message = "Дата від'їзду обов'язкова")
        LocalDate departureDate
) {
}
