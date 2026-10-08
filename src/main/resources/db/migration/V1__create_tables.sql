-- Подорожі
CREATE TABLE trips (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(100)   NOT NULL,
    description VARCHAR(1000),
    start_date  DATE           NOT NULL,
    end_date    DATE           NOT NULL,
    budget      NUMERIC(12, 2),
    status      VARCHAR(20)    NOT NULL DEFAULT 'PLANNED',
    created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_trip_dates  CHECK (end_date >= start_date),
    CONSTRAINT chk_trip_budget CHECK (budget IS NULL OR budget >= 0)
);

-- Міста в подорожі
CREATE TABLE destinations (
    id             BIGSERIAL PRIMARY KEY,
    city           VARCHAR(100) NOT NULL,
    country        VARCHAR(100) NOT NULL,
    arrival_date   DATE         NOT NULL,
    departure_date DATE         NOT NULL,
    trip_id        BIGINT       NOT NULL REFERENCES trips (id) ON DELETE CASCADE,
    CONSTRAINT chk_destination_dates CHECK (departure_date >= arrival_date)
);

-- Місця для відвідування в місті
CREATE TABLE places (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(150)   NOT NULL,
    category       VARCHAR(20)    NOT NULL,
    planned_date   DATE,
    cost           NUMERIC(10, 2),
    visited        BOOLEAN        NOT NULL DEFAULT FALSE,
    rating         INTEGER,
    destination_id BIGINT         NOT NULL REFERENCES destinations (id) ON DELETE CASCADE,
    CONSTRAINT chk_place_cost   CHECK (cost IS NULL OR cost >= 0),
    CONSTRAINT chk_place_rating CHECK (rating IS NULL OR rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_destinations_trip_id ON destinations (trip_id);
CREATE INDEX idx_places_destination_id ON places (destination_id);
