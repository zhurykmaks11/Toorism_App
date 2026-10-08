package org.example.turism_app.service;

import lombok.RequiredArgsConstructor;
import org.example.turism_app.dto.PlaceRequest;
import org.example.turism_app.dto.PlaceResponse;
import org.example.turism_app.exception.BadRequestException;
import org.example.turism_app.exception.NotFoundException;
import org.example.turism_app.model.Destination;
import org.example.turism_app.model.Place;
import org.example.turism_app.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final DestinationService destinationService;

    public List<PlaceResponse> getByDestination(Long destinationId) {
        destinationService.findDestination(destinationId); // 404, якщо міста немає
        return placeRepository.findByDestinationIdOrderByPlannedDateAsc(destinationId)
                .stream().map(PlaceResponse::from).toList();
    }

    public PlaceResponse getById(Long id) {
        return PlaceResponse.from(findPlace(id));
    }

    @Transactional
    public PlaceResponse create(Long destinationId, PlaceRequest request) {
        Destination destination = destinationService.findDestination(destinationId);
        validate(request, destination);

        Place place = new Place();
        place.setDestination(destination);
        apply(place, request);
        return PlaceResponse.from(placeRepository.save(place));
    }

    @Transactional
    public PlaceResponse update(Long id, PlaceRequest request) {
        Place place = findPlace(id);
        validate(request, place.getDestination());
        apply(place, request);
        return PlaceResponse.from(place);
    }

    @Transactional
    public PlaceResponse markVisited(Long id, Integer rating) {
        Place place = findPlace(id);
        place.setVisited(true);
        if (rating != null) {
            place.setRating(rating);
        }
        return PlaceResponse.from(place);
    }

    @Transactional
    public void delete(Long id) {
        placeRepository.delete(findPlace(id));
    }

    private Place findPlace(Long id) {
        return placeRepository.findById(id).orElseThrow(() -> new NotFoundException("Місце", id));
    }

    private void apply(Place place, PlaceRequest request) {
        place.setName(request.name().trim());
        place.setCategory(request.category());
        place.setPlannedDate(request.plannedDate());
        place.setCost(request.cost());
        place.setVisited(Boolean.TRUE.equals(request.visited()));
        place.setRating(request.rating());
    }

    private void validate(PlaceRequest request, Destination destination) {
        if (request.plannedDate() != null && (request.plannedDate().isBefore(destination.getArrivalDate())
                || request.plannedDate().isAfter(destination.getDepartureDate()))) {
            throw new BadRequestException("Запланована дата має бути в межах перебування в місті ("
                    + destination.getArrivalDate() + " — " + destination.getDepartureDate() + ")");
        }
        if (request.rating() != null && !Boolean.TRUE.equals(request.visited())) {
            throw new BadRequestException("Оцінку можна поставити лише відвіданому місцю (visited = true)");
        }
    }
}
