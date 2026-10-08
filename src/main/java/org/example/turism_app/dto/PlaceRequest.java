package org.example.turism_app.dto;

import jakarta.validation.constraints.*;
import org.example.turism_app.model.PlaceCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PlaceRequest(
        @NotBlank(message = "Назва місця обов'язкова")
        @Size(max = 150, message = "Назва не довша за 150 символів")
        String name,

        @NotNull(message = "Категорія обов'язкова (MUSEUM, FOOD, NATURE, LANDMARK, OTHER)")
        PlaceCategory category,

        LocalDate plannedDate,

        @PositiveOrZero(message = "Вартість не може бути від'ємною")
        BigDecimal cost,

        Boolean visited,

        @Min(value = 1, message = "Оцінка від 1 до 5")
        @Max(value = 5, message = "Оцінка від 1 до 5")
        Integer rating
) {
}
