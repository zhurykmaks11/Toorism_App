package org.example.turism_app.dto;

import jakarta.validation.constraints.*;
import org.example.turism_app.model.TripStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TripRequest(
        @NotBlank(message = "Назва обов'язкова")
        @Size(max = 100, message = "Назва не довша за 100 символів")
        String title,

        @Size(max = 1000, message = "Опис не довший за 1000 символів")
        String description,

        @NotNull(message = "Дата початку обов'язкова")
        LocalDate startDate,

        @NotNull(message = "Дата завершення обов'язкова")
        LocalDate endDate,

        @PositiveOrZero(message = "Бюджет не може бути від'ємним")
        BigDecimal budget,

        TripStatus status
) {
}
