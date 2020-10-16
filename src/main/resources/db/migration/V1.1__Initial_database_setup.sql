CREATE TABLE cars
(
    id    BIGSERIAL PRIMARY KEY,
    brand VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL
);

CREATE TABLE parts
(
    part_no      BIGINT PRIMARY KEY,
    car_id       BIGINT REFERENCES cars (id) ON DELETE CASCADE,
    manufacturer VARCHAR(255) NOT NULL,
    description  TEXT         NOT NULL
);