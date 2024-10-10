CREATE TABLE map_tile
(
    _id        SERIAL PRIMARY KEY,
    x          INTEGER NOT NULL,
    y          INTEGER NOT NULL,
    type       TEXT    NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (NOW() AT TIME ZONE 'CET')
);
