DO
$$
    BEGIN
        IF EXISTS
            (SELECT 1 from pg_roles where rolname = 'cloudsqliamuser')
        THEN
            GRANT USAGE ON SCHEMA public TO cloudsqliamuser;
            GRANT SELECT ON ALL TABLES IN SCHEMA public TO cloudsqliamuser;
            ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO cloudsqliamuser;
        END IF;
    END
$$;

CREATE TABLE IF NOT EXISTS søker
(
    id       UUID PRIMARY KEY,
    ident    VARCHAR NOT NULL UNIQUE,
    tilstand VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS søknad
(
    id              UUID PRIMARY KEY,
    søker_id        UUID                     NOT NULL REFERENCES søker (id),
    journalpost_id  BIGINT                   NOT NULL,
    dokumentinfo_id BIGINT                   NOT NULL,
    opprettet_dato  TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
