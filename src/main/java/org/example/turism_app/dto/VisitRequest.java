package org.example.turism_app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record VisitRequest(
        @Min(value = 1, message = "Оцінка від 1 до 5")
        @Max(value = 5, message = "Оцінка від 1 до 5")
        Integer rating
) {
}
