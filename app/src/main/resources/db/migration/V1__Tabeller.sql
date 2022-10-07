DO
$$
    BEGIN
        IF
            EXISTS
                (SELECT 1 from pg_roles where rolname = 'cloudsqliamuser')
        THEN
            GRANT USAGE ON SCHEMA public TO cloudsqliamuser;
            GRANT
                SELECT
                ON ALL TABLES IN SCHEMA public TO cloudsqliamuser;
            ALTER
                DEFAULT PRIVILEGES IN SCHEMA public GRANT
                SELECT
                ON TABLES TO cloudsqliamuser;
        END IF;
    END
$$;

CREATE TABLE søker
(
    id          UUID PRIMARY KEY,
    ident       VARCHAR                  NOT NULL UNIQUE,
    tilstand    VARCHAR                  NOT NULL,
    sist_endret TIMESTAMP WITH TIME ZONE NOT NULL,
    opprettet   TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE søknad
(
    id                  UUID PRIMARY KEY,
    søker_id            UUID                     NOT NULL REFERENCES søker (id),
    søknad_id           VARCHAR                  NOT NULL,
    ident               VARCHAR                  NOT NULL,
    fornavn             VARCHAR                  NULL,
    etternavn           VARCHAR                  NULL,
    deltar_kvp          BOOLEAN                  NOT NULL,
    deltar_intro        BOOLEAN                  NULL,
    intro_fom           DATE                     NULL,
    intro_tom           DATE                     NULL,
    institusjon_opphold BOOLEAN                  NULL,
    institusjon_type    VARCHAR                  NULL,
    fritekst            VARCHAR                  NULL,
    journalpost_id      VARCHAR                  NOT NULL,
    dokumentinfo_id     VARCHAR                  NOT NULL,
    opprettet           TIMESTAMP WITH TIME ZONE NULL,
    tidsstempel_hos_oss TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE barnetillegg
(
    id                UUID PRIMARY KEY,
    søknad_id         UUID    NOT NULL REFERENCES søknad (id),
    ident             VARCHAR NULL,
    fødselsdato       DATE    NULL,
    fornavn           VARCHAR NULL,
    mellomnavn        VARCHAR NULL,
    etternavn         VARCHAR NULL,
    alder             INT     NOT NULL,
    land              VARCHAR NOT NULL,
    søkt_barnetillegg BOOLEAN NOT NULL
);

CREATE TABLE brukertiltak
(
    id            UUID PRIMARY KEY,
    søknad_id     UUID    NOT NULL REFERENCES søknad (id),
    tiltakskode   VARCHAR NULL,
    arrangoernavn VARCHAR NULL,
    beskrivelse   VARCHAR NULL,
    startdato     DATE    NOT NULL,
    sluttdato     DATE    NOT NULL,
    adresse       VARCHAR NULL,
    postnummer    VARCHAR NULL,
    antall_dager  INT     NOT NULL
);

CREATE TABLE arenatiltak
(
    id                      UUID PRIMARY KEY,
    søknad_id               UUID    NOT NULL REFERENCES søknad (id),
    arena_id                VARCHAR NOT NULL,
    arrangoernavn           VARCHAR NULL,
    har_sluttdato_fra_arena BOOLEAN NOT NULL,
    tiltakskode             VARCHAR NOT NULL,
    er_i_endre_status       BOOLEAN NOT NULL,
    opprinnelig_startdato   DATE    NOT NULL,
    opprinnelig_sluttdato   DATE    NULL,
    startdato               DATE    NOT NULL,
    sluttdato               DATE    NULL
);

CREATE TABLE trygdogpensjon
(
    id        UUID PRIMARY KEY,
    søknad_id UUID    NOT NULL REFERENCES søknad (id),
    utbetaler VARCHAR NOT NULL,
    prosent   INT     NULL,
    fom       DATE    NULL,
    tom       DATE    NULL
);

CREATE TABLE personopplysninger_søker
(
    id                  UUID PRIMARY KEY,
    søker_id            UUID                     NOT NULL REFERENCES søker (id),
    ident               VARCHAR                  NOT NULL,
    fødselsdato         DATE                     NOT NULL,
    fornavn             VARCHAR                  NOT NULL,
    mellomnavn          VARCHAR                  NULL,
    etternavn           VARCHAR                  NOT NULL,
    fortrolig           BOOLEAN                  NOT NULL,
    strengt_fortrolig   BOOLEAN                  NOT NULL,
    skjermet            BOOLEAN                  NULL,
    kommune             VARCHAR                  NULL,
    bydel               VARCHAR                  NULL,
    tidsstempel_hos_oss TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE personopplysninger_barn_med_ident
(
    id                  UUID PRIMARY KEY,
    søker_id            UUID                     NOT NULL REFERENCES søker (id),
    ident               VARCHAR                  NOT NULL,
    fødselsdato         DATE                     NOT NULL,
    fornavn             VARCHAR                  NOT NULL,
    mellomnavn          VARCHAR                  NULL,
    etternavn           VARCHAR                  NOT NULL,
    fortrolig           BOOLEAN                  NOT NULL,
    strengt_fortrolig   BOOLEAN                  NOT NULL,
    land                VARCHAR                  NULL,
    tidsstempel_hos_oss TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE personopplysninger_barn_uten_ident
(
    id                  UUID PRIMARY KEY,
    søker_id            UUID                     NOT NULL REFERENCES søker (id),
    fødselsdato         DATE                     NULL,
    fornavn             VARCHAR                  NULL,
    mellomnavn          VARCHAR                  NULL,
    etternavn           VARCHAR                  NULL,
    tidsstempel_hos_oss TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE tiltaksaktivitet
(
    id                     UUID PRIMARY KEY,
    søker_id               UUID                     NOT NULL REFERENCES søker (id),
    tiltak                 VARCHAR                  NOT NULL,
    aktivitet_id           VARCHAR                  NOT NULL,
    tiltak_lokalt_navn     VARCHAR                  NULL,
    arrangør               VARCHAR                  NULL,
    bedriftsnummer         VARCHAR                  NULL,
    deltakelse_periode_fom DATE                     NULL,
    deltakelse_periode_tom DATE                     NULL,
    deltakelse_prosent     FLOAT                    NULL,
    deltaker_status        VARCHAR                  NOT NULL,
    status_sist_endret     DATE                     NULL,
    begrunnelse_innsøking  VARCHAR                  NULL,
    antall_dager_per_uke   FLOAT                    NULL,
    tidsstempel_hos_oss    TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE ytelsesak
(
    id                    UUID PRIMARY KEY,
    søker_id              UUID                     NOT NULL REFERENCES søker (id),
    fom_gyldighetsperiode TIMESTAMP WITH TIME ZONE NOT NULL,
    tom_gyldighetsperiode TIMESTAMP WITH TIME ZONE NULL,
    dato_krav_mottatt     DATE                     NULL,
    data_krav_mottatt     VARCHAR                  NULL,
    fagsystem_sak_id      INT                      NULL,
    status                VARCHAR                  NULL,
    ytelsestype           VARCHAR                  NULL,
    antall_dager_igjen    INT                      NULL,
    antall_uker_igjen     INT                      NULL,
    tidsstempel_hos_oss   TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE ytelsevedtak
(
    id                     UUID PRIMARY KEY,
    ytelsesak_id           UUID    NOT NULL REFERENCES ytelsesak (id),
    beslutnings_dato       DATE    NULL,
    periodetype_for_ytelse VARCHAR NULL,
    vedtaksperiode_fom     DATE    NULL,
    vedtaksperiode_tom     DATE    NULL,
    vedtaks_type           VARCHAR NULL,
    status                 VARCHAR NULL
);

CREATE TABLE aktivitet
(
    id               UUID PRIMARY KEY,
    søker_id         UUID                     NOT NULL REFERENCES søker (id),
    type             VARCHAR                  NULL,
    alvorlighetsgrad INT                      NOT NULL,
    label            CHAR(1)                  NOT NULL,
    melding          VARCHAR                  NOT NULL,
    tidsstempel      TIMESTAMP WITH TIME ZONE NOT NULL,
    detaljer         JSONB                    NULL,
    kontekster       JSONB                    NOT NULL
);
