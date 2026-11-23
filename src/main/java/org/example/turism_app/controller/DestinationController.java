package org.example.turism_app.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.turism_app.dto.DestinationRequest;
import org.example.turism_app.dto.DestinationResponse;
import org.example.turism_app.service.DestinationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Міста в подорожі.
 *
 * <p><b>Лаба №6 — доступ задано анотаціями на методах</b> (а не URL-правилами в SecurityConfig):
 * <ul>
 *   <li>{@code @PreAuthorize} — вираз SpEL, найгнучкіший варіант;</li>
 *   <li>{@code @Secured} — список ролей Spring Security (з префіксом ROLE_);</li>
 *   <li>{@code @RolesAllowed} — стандартна анотація Jakarta (JSR-250), ролі без префікса.</li>
 * </ul>
 * Якщо роль не підходить, Spring Security не викликає метод і повертає 403 Forbidden.</p>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DestinationController {

    private final DestinationService destinationService;

    /** Читати міста можуть усі три ролі. */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPERADMIN')")
    @GetMapping("/trips/{tripId}/destinations")
    public List<DestinationResponse> getByTrip(@PathVariable Long tripId) {
        return destinationService.getByTrip(tripId);
    }

    /** Додавати місто — лише ADMIN і SUPERADMIN. */
    @Secured({"ROLE_ADMIN", "ROLE_SUPERADMIN"})
    @PostMapping("/trips/{tripId}/destinations")
    public ResponseEntity<DestinationResponse> create(@PathVariable Long tripId,
                                                      @Valid @RequestBody DestinationRequest request) {
        DestinationResponse created = destinationService.create(tripId, request);
        return ResponseEntity.created(URI.create("/api/destinations/" + created.id())).body(created);
    }

    /** Читати одне місто можуть усі три ролі. */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPERADMIN')")
    @GetMapping("/destinations/{id}")
    public DestinationResponse getById(@PathVariable Long id) {
        return destinationService.getById(id);
    }

    /** Редагувати місто — лише ADMIN і SUPERADMIN. */
    @Secured({"ROLE_ADMIN", "ROLE_SUPERADMIN"})
    @PutMapping("/destinations/{id}")
    public DestinationResponse update(@PathVariable Long id, @Valid @RequestBody DestinationRequest request) {
        return destinationService.update(id, request);
    }

    /** Видаляти місто (разом з його місцями) — лише SUPERADMIN. */
    @RolesAllowed("SUPERADMIN")
    @DeleteMapping("/destinations/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        destinationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
