CREATE SCHEMA IF NOT EXISTS auth;

CREATE TABLE IF NOT EXISTS auth._user (
    uuid        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_name   TEXT NOT NULL UNIQUE,
    first_name  TEXT,
    last_name   TEXT,
    email       TEXT NOT NULL UNIQUE,
    password    TEXT NOT NULL,
    role        TEXT NOT NULL
);
