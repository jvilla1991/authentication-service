-- Objects are created in the schema configured via spring.flyway.default-schema
-- (`auth`), which the tm_<env>_auth role owns. No hard-coded schema name.
CREATE TABLE IF NOT EXISTS _user (
    uuid        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_name   TEXT NOT NULL UNIQUE,
    first_name  TEXT,
    last_name   TEXT,
    email       TEXT NOT NULL UNIQUE,
    password    TEXT NOT NULL,
    role        TEXT NOT NULL
);
