package org.example.turism_app.controller;

import org.example.turism_app.model.Trip;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final List<Trip> trips = List.of(
            new Trip(1L, "Карпати", "Похід на Говерлу", LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 15), 8000.0),
            new Trip(2L, "Львів", "Кава і старе місто", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 3), 5000.0),
            new Trip(3L, "Одеса", "Море і Привоз", LocalDate.of(2026, 8, 5), LocalDate.of(2026, 8, 12), 12000.0)
    );

    @GetMapping
    public List<Trip> getAll() {
        return trips;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trip> getById(@PathVariable Long id) {
        return trips.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
