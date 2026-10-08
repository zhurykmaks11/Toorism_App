package org.example.turism_app.repository;

import org.example.turism_app.model.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    List<Place> findByDestinationIdOrderByPlannedDateAsc(Long destinationId);
}
