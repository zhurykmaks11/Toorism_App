package org.example.turism_app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.turism_app.dto.DestinationRequest;
import org.example.turism_app.dto.DestinationResponse;
import org.example.turism_app.service.DestinationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;

    @GetMapping("/trips/{tripId}/destinations")
    public List<DestinationResponse> getByTrip(@PathVariable Long tripId) {
        return destinationService.getByTrip(tripId);
    }

    @PostMapping("/trips/{tripId}/destinations")
    public ResponseEntity<DestinationResponse> create(@PathVariable Long tripId,
                                                      @Valid @RequestBody DestinationRequest request) {
        DestinationResponse created = destinationService.create(tripId, request);
        return ResponseEntity.created(URI.create("/api/destinations/" + created.id())).body(created);
    }

    @GetMapping("/destinations/{id}")
    public DestinationResponse getById(@PathVariable Long id) {
        return destinationService.getById(id);
    }

    @PutMapping("/destinations/{id}")
    public DestinationResponse update(@PathVariable Long id, @Valid @RequestBody DestinationRequest request) {
        return destinationService.update(id, request);
    }

    @DeleteMapping("/destinations/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        destinationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
