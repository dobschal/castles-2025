CREATE TABLE castles_user
(
    id         SERIAL PRIMARY KEY,
    username   TEXT NOT NULL UNIQUE,
    "password" TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (NOW() AT TIME ZONE 'CET')
);
