CREATE TABLE building
(
    id         SERIAL PRIMARY KEY,
    x          INTEGER NOT NULL,
    y          INTEGER NOT NULL,
    type       TEXT    NOT NULL,
    user_id    INTEGER,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (NOW() AT TIME ZONE 'CET')
);
