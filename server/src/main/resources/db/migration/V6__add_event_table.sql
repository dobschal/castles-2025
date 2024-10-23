CREATE TABLE "event"
(
    id          SERIAL PRIMARY KEY,
    x           INTEGER NOT NULL,
    y           INTEGER NOT NULL,
    type        TEXT    NOT NULL,
    unit_id     INTEGER,
    building_id INTEGER,
    user1_id    INTEGER,
    user2_id    INTEGER,
    created_at  TIMESTAMP WITHOUT TIME ZONE DEFAULT (NOW() AT TIME ZONE 'CET')
);
