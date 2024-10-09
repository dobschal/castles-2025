CREATE TABLE castles_user
(
    _id        SERIAL PRIMARY KEY,
    name       TEXT NOT NULL,
    password   TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (NOW() AT TIME ZONE 'CET')
);
