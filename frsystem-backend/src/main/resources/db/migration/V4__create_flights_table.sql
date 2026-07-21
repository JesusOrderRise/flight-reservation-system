CREATE TYPE flight_status AS ENUM ('ACTIVE', 'FINISHED', 'CANCELED');

CREATE TABLE IF NOT EXISTS flight (
    id BIGSERIAL PRIMARY KEY,
    flight_number VARCHAR(20) NOT NULL UNIQUE,
    airplane_id BIGINT NOT NULL,
    departure_airport_id BIGINT NOT NULL,
    arrival_airport_id BIGINT NOT NULL,
    departure_time TIMESTAMP NOT NULL,
    arrival_time TIMESTAMP NOT NULL,
    status flight_status NOT NULL,
    last_update TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_flight_airplane FOREIGN KEY (airplane_id) REFERENCES airplane(id),
    CONSTRAINT fk_flight_departure_airport FOREIGN KEY (departure_airport_id) REFERENCES airport(id),
    CONSTRAINT fk_flight_arrival_airport FOREIGN KEY (arrival_airport_id) REFERENCES airport(id),
    CONSTRAINT chk_departure_arrival_time CHECK (arrival_time > departure_time),
    CONSTRAINT chk_different_airports CHECK (departure_airport_id != arrival_airport_id)
);


