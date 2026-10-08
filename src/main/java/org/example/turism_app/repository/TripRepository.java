package org.example.turism_app.repository;

import org.example.turism_app.model.Trip;
import org.example.turism_app.model.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findAllByOrderByStartDateAsc();

    List<Trip> findByStatusOrderByStartDateAsc(TripStatus status);
}
