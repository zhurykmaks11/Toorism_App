package org.example.turism_app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.turism_app.dto.PlaceRequest;
import org.example.turism_app.dto.PlaceResponse;
import org.example.turism_app.dto.VisitRequest;
import org.example.turism_app.service.PlaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @GetMapping("/destinations/{destinationId}/places")
    public List<PlaceResponse> getByDestination(@PathVariable Long destinationId) {
        return placeService.getByDestination(destinationId);
    }

    @PostMapping("/destinations/{destinationId}/places")
    public ResponseEntity<PlaceResponse> create(@PathVariable Long destinationId,
                                                @Valid @RequestBody PlaceRequest request) {
        PlaceResponse created = placeService.create(destinationId, request);
        return ResponseEntity.created(URI.create("/api/places/" + created.id())).body(created);
    }

    @GetMapping("/places/{id}")
    public PlaceResponse getById(@PathVariable Long id) {
        return placeService.getById(id);
    }

    @PutMapping("/places/{id}")
    public PlaceResponse update(@PathVariable Long id, @Valid @RequestBody PlaceRequest request) {
        return placeService.update(id, request);
    }

    @PatchMapping("/places/{id}/visit")
    public PlaceResponse markVisited(@PathVariable Long id,
                                     @Valid @RequestBody(required = false) VisitRequest request) {
        return placeService.markVisited(id, request == null ? null : request.rating());
    }

    @DeleteMapping("/places/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        placeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
