package org.example.turism_app.repository;

import org.example.turism_app.model.Destination;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DestinationRepository extends JpaRepository<Destination, Long> {

    List<Destination> findByTripIdOrderByArrivalDateAsc(Long tripId);
}
