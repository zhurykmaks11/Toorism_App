package org.example.turism_app.service;

import lombok.RequiredArgsConstructor;
import org.example.turism_app.dto.TripRequest;
import org.example.turism_app.dto.TripResponse;
import org.example.turism_app.exception.BadRequestException;
import org.example.turism_app.exception.NotFoundException;
import org.example.turism_app.model.Destination;
import org.example.turism_app.model.Trip;
import org.example.turism_app.model.TripStatus;
import org.example.turism_app.repository.TripRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripService {

    private final TripRepository tripRepository;

    public List<TripResponse> getAll(TripStatus status) {
        List<Trip> trips = (status == null)
                ? tripRepository.findAllByOrderByStartDateAsc()
                : tripRepository.findByStatusOrderByStartDateAsc(status);
        return trips.stream().map(TripResponse::from).toList();
    }

    public TripResponse getById(Long id) {
        return TripResponse.from(findTrip(id));
    }

    @Transactional
    public TripResponse create(TripRequest request) {
        validateDates(request);
        Trip trip = new Trip();
        apply(trip, request);
        if (request.status() == null) {
            trip.setStatus(TripStatus.PLANNED);
        }
        return TripResponse.from(tripRepository.save(trip));
    }

    @Transactional
    public TripResponse update(Long id, TripRequest request) {
        validateDates(request);
        Trip trip = findTrip(id);

        // Нові дати подорожі не повинні "відрізати" вже додані міста
        for (Destination d : trip.getDestinations()) {
            if (d.getArrivalDate().isBefore(request.startDate()) || d.getDepartureDate().isAfter(request.endDate())) {
                throw new BadRequestException("Місто '" + d.getCity() + "' (" + d.getArrivalDate() + " — "
                        + d.getDepartureDate() + ") виходить за нові дати подорожі");
            }
        }

        apply(trip, request);
        return TripResponse.from(trip);
    }

    @Transactional
    public void delete(Long id) {
        Trip trip = findTrip(id);
        tripRepository.delete(trip); // міста і місця видаляються каскадно
    }

    public Trip findTrip(Long id) {
        return tripRepository.findById(id).orElseThrow(() -> new NotFoundException("Подорож", id));
    }

    private void apply(Trip trip, TripRequest request) {
        trip.setTitle(request.title().trim());
        trip.setDescription(request.description());
        trip.setStartDate(request.startDate());
        trip.setEndDate(request.endDate());
        trip.setBudget(request.budget());
        if (request.status() != null) {
            trip.setStatus(request.status());
        }
    }

    private void validateDates(TripRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new BadRequestException("Дата завершення не може бути раніше дати початку");
        }
    }
}
