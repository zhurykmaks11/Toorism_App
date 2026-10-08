package org.example.turism_app.service;

import lombok.RequiredArgsConstructor;
import org.example.turism_app.dto.DestinationRequest;
import org.example.turism_app.dto.DestinationResponse;
import org.example.turism_app.exception.BadRequestException;
import org.example.turism_app.exception.NotFoundException;
import org.example.turism_app.model.Destination;
import org.example.turism_app.model.Place;
import org.example.turism_app.model.Trip;
import org.example.turism_app.repository.DestinationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DestinationService {

    private final DestinationRepository destinationRepository;
    private final TripService tripService;

    public List<DestinationResponse> getByTrip(Long tripId) {
        tripService.findTrip(tripId); // 404, якщо подорожі немає
        return destinationRepository.findByTripIdOrderByArrivalDateAsc(tripId)
                .stream().map(DestinationResponse::from).toList();
    }

    public DestinationResponse getById(Long id) {
        return DestinationResponse.from(findDestination(id));
    }

    @Transactional
    public DestinationResponse create(Long tripId, DestinationRequest request) {
        Trip trip = tripService.findTrip(tripId);
        validateDates(request, trip);

        Destination destination = new Destination();
        destination.setTrip(trip);
        apply(destination, request);
        return DestinationResponse.from(destinationRepository.save(destination));
    }

    @Transactional
    public DestinationResponse update(Long id, DestinationRequest request) {
        Destination destination = findDestination(id);
        validateDates(request, destination.getTrip());

        for (Place p : destination.getPlaces()) {
            if (p.getPlannedDate() != null && (p.getPlannedDate().isBefore(request.arrivalDate())
                    || p.getPlannedDate().isAfter(request.departureDate()))) {
                throw new BadRequestException("Місце '" + p.getName() + "' заплановане на " + p.getPlannedDate()
                        + " і виходить за нові дати перебування в місті");
            }
        }

        apply(destination, request);
        return DestinationResponse.from(destination);
    }

    @Transactional
    public void delete(Long id) {
        destinationRepository.delete(findDestination(id)); // місця видаляються каскадно
    }

    public Destination findDestination(Long id) {
        return destinationRepository.findById(id).orElseThrow(() -> new NotFoundException("Місто", id));
    }

    private void apply(Destination destination, DestinationRequest request) {
        destination.setCity(request.city().trim());
        destination.setCountry(request.country().trim());
        destination.setArrivalDate(request.arrivalDate());
        destination.setDepartureDate(request.departureDate());
    }

    private void validateDates(DestinationRequest request, Trip trip) {
        if (request.departureDate().isBefore(request.arrivalDate())) {
            throw new BadRequestException("Дата від'їзду не може бути раніше дати прибуття");
        }
        if (request.arrivalDate().isBefore(trip.getStartDate()) || request.departureDate().isAfter(trip.getEndDate())) {
            throw new BadRequestException("Дати перебування в місті мають бути в межах подорожі ("
                    + trip.getStartDate() + " — " + trip.getEndDate() + ")");
        }
    }
}
