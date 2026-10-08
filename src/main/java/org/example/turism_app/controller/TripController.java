package org.example.turism_app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.turism_app.dto.TripRequest;
import org.example.turism_app.dto.TripResponse;
import org.example.turism_app.model.TripStatus;
import org.example.turism_app.service.TripService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @GetMapping
    public List<TripResponse> getAll(@RequestParam(required = false) TripStatus status) {
        return tripService.getAll(status);
    }

    @GetMapping("/{id}")
    public TripResponse getById(@PathVariable Long id) {
        return tripService.getById(id);
    }

    @PostMapping
    public ResponseEntity<TripResponse> create(@Valid @RequestBody TripRequest request) {
        TripResponse created = tripService.create(request);
        return ResponseEntity.created(URI.create("/api/trips/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public TripResponse update(@PathVariable Long id, @Valid @RequestBody TripRequest request) {
        return tripService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tripService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
