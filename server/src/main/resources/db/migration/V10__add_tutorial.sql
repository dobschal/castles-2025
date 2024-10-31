CREATE TABLE tutorial
(
    id         SERIAL PRIMARY KEY,
    type       TEXT NOT NULL,
    status     TEXT NOT NULL,
    user_id    INTEGER,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT (NOW() AT TIME ZONE 'CET')
);
