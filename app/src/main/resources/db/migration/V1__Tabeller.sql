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
    id          UUID PRIMARY KEY,
    ident       VARCHAR                  NOT NULL UNIQUE,
    tilstand    VARCHAR                  NOT NULL,
    sist_endret TIMESTAMP WITH TIME ZONE NOT NULL,
    opprettet   TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS søknad
(
    id                  UUID PRIMARY KEY,
    søker_id            UUID                     NOT NULL REFERENCES søker (id),
    søknad_id           VARCHAR                  NOT NULL,
    ident               VARCHAR                  NOT NULL,
    fornavn             VARCHAR                  NULL,
    etternavn           VARCHAR                  NULL,
    deltar_kvp          BOOLEAN                  NOT NULL,
    deltar_intro        BOOLEAN                  NULL,
    institusjon_opphold BOOLEAN                  NULL,
    institusjon_type    VARCHAR                  NULL,
    fritekst            VARCHAR                  NULL,
    journalpost_id      VARCHAR                  NOT NULL,
    dokumentinfo_id     VARCHAR                  NOT NULL,
    tidsstempel_kilde   TIMESTAMP WITH TIME ZONE NOT NULL,
    tidsstempel_hos_oss TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE IF NOT EXISTS barnetillegg
(
    id        UUID PRIMARY KEY,
    søknad_id UUID    NOT NULL REFERENCES søknad (id),
    ident     VARCHAR NOT NULL,
    fornavn   VARCHAR NULL,
    etternavn VARCHAR NULL,
    alder     INT     NOT NULL,
    land      VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS brukertiltak
(
    id            UUID PRIMARY KEY,
    søknad_id     UUID    NOT NULL REFERENCES søknad (id),
    tiltakstype   VARCHAR NULL,
    arrangoernavn VARCHAR NULL,
    beskrivelse   VARCHAR NULL,
    fom           DATE    NULL,
    tom           DATE    NULL,
    adresse       VARCHAR NULL,
    postnummer    VARCHAR NULL,
    antall_dager  INT     NOT NULL
);

CREATE TABLE IF NOT EXISTS arenatiltak
(
    id                      UUID PRIMARY KEY,
    søknad_id               UUID    NOT NULL REFERENCES søknad (id),
    arena_id                VARCHAR NULL,
    arrangoer               VARCHAR NULL,
    har_sluttdato_fra_arena BOOLEAN NULL,
    navn                    VARCHAR NULL,
    er_i_endre_status       BOOLEAN NULL,
    opprinnelig_startdato   DATE    NULL,
    opprinnelig_sluttdato   DATE    NULL,
    startdato               DATE    NULL,
    sluttdato               DATE    NULL
);

CREATE TABLE IF NOT EXISTS trygdogpensjon
(
    id        UUID PRIMARY KEY,
    søknad_id UUID    NOT NULL REFERENCES søknad (id),
    utbetaler VARCHAR NOT NULL,
    prosent   INT     NULL,
    fom       DATE    NOT NULL,
    tom       DATE    NULL
);
