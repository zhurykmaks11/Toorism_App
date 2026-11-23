package org.example.turism_app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.turism_app.dto.PlaceRequest;
import org.example.turism_app.dto.PlaceResponse;
import org.example.turism_app.dto.VisitRequest;
import org.example.turism_app.service.PlaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Місця для відвідування в місті.
 *
 * <p><b>Лаба №6 — доступ задано анотаціями {@code @PreAuthorize}</b> на кожному методі.
 * Особливість: позначити місце відвіданим (PATCH /visit) може навіть USER — мандрівник
 * ставить оцінку місцю, хоча редагувати чи видаляти його не може. За URL-правилами
 * лаби №5 це було б неможливо (там PATCH дозволено лише ADMIN і SUPERADMIN).</p>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    /** Читати місця може будь-який авторизований користувач. */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/destinations/{destinationId}/places")
    public List<PlaceResponse> getByDestination(@PathVariable Long destinationId) {
        return placeService.getByDestination(destinationId);
    }

    /** Додавати місце — ADMIN і SUPERADMIN. */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @PostMapping("/destinations/{destinationId}/places")
    public ResponseEntity<PlaceResponse> create(@PathVariable Long destinationId,
                                                @Valid @RequestBody PlaceRequest request) {
        PlaceResponse created = placeService.create(destinationId, request);
        return ResponseEntity.created(URI.create("/api/places/" + created.id())).body(created);
    }

    /** Читати одне місце — будь-який авторизований користувач. */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/places/{id}")
    public PlaceResponse getById(@PathVariable Long id) {
        return placeService.getById(id);
    }

    /** Редагувати місце — ADMIN і SUPERADMIN. */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @PutMapping("/places/{id}")
    public PlaceResponse update(@PathVariable Long id, @Valid @RequestBody PlaceRequest request) {
        return placeService.update(id, request);
    }

    /** Позначити відвіданим і поставити оцінку — усі три ролі, зокрема USER. */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPERADMIN')")
    @PatchMapping("/places/{id}/visit")
    public PlaceResponse markVisited(@PathVariable Long id,
                                     @Valid @RequestBody(required = false) VisitRequest request) {
        return placeService.markVisited(id, request == null ? null : request.rating());
    }

    /** Видаляти місце — лише SUPERADMIN. */
    @PreAuthorize("hasRole('SUPERADMIN')")
    @DeleteMapping("/places/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        placeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
