CREATE TABLE events (
    id    SERIAL PRIMARY KEY,
    name  VARCHAR NOT NULL,
    note  VARCHAR,
    time  TIMESTAMP NOT NULL
);
