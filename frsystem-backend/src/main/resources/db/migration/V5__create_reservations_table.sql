CREATE TYPE reservation_status AS ENUM ('CONFIRMED', 'CANCELED');


CREATE TABLE IF NOT EXISTS reservation (
    id BIGSERIAL PRIMARY KEY,
    flight_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    status reservation_status NOT NULL,

    CONSTRAINT fk_reservation_flight FOREIGN KEY (flight_id) REFERENCES flight(id),
    CONSTRAINT fk_reservation_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uk_flight_seat UNIQUE (flight_id, seat_number)
);