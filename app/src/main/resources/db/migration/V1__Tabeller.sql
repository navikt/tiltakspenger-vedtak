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

create TABLE sak
(
    id                      VARCHAR                  PRIMARY KEY,
    ident                   VARCHAR                  NOT NULL,
    saksnummer              VARCHAR                  NOT NULL,
    fom                     DATE                     NOT NULL,
    tom                     DATE                     NOT NULL,
    sist_endret             TIMESTAMP WITH TIME ZONE NOT NULL,
    opprettet               TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE behandling
(
    id                      VARCHAR                  PRIMARY KEY,
    sakId                   VARCHAR                  NOT NULL REFERENCES sak (id),
    fom                     DATE                     NOT NULL,
    tom                     DATE                     NOT NULL,
    tilstand                VARCHAR                  NOT NULL,
    status                  VARCHAR                  NOT NULL,
    saksbehandler           VARCHAR                  NULL,
    beslutter               VARCHAR                  NULL,
    iverksattTidspunkt      TIMESTAMP WITH TIME ZONE NULL,
    sist_endret             TIMESTAMP WITH TIME ZONE NOT NULL,
    opprettet               TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE vedtak
(
    id                      VARCHAR                  PRIMARY KEY,
    sak_id                  VARCHAR                  NULL REFERENCES sak (id),
    behandling_id           VARCHAR                  NULL REFERENCES behandling (id),
    vedtakstype             VARCHAR                  NOT NULL,
    vedtaksdato             TIMESTAMP WITH TIME ZONE NOT NULL,
    fom                     DATE                     NOT NULL,
    tom                     DATE                     NOT NULL,
    saksbehandler           VARCHAR                  NOT NULL,
    beslutter               VARCHAR                  NOT NULL,
    opprettet               TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE utfallsperiode
(
    id                  VARCHAR PRIMARY KEY,
    behandling_id       VARCHAR                  NOT NULL REFERENCES behandling (id),
    vedtak_id           VARCHAR                  REFERENCES vedtak (id),
    fom                 DATE                     NOT NULL,
    tom                 DATE                     NOT NULL,
    antall_barn         INT                      NOT NULL,
    utfall              VARCHAR                  NOT NULL
);

CREATE TABLE saksopplysning
(
    id                      VARCHAR                  PRIMARY KEY,
    behandlingId            VARCHAR                  REFERENCES behandling (id),
    vedtakId                VARCHAR                  REFERENCES vedtak (id),
    fom                     DATE                     NOT NULL,
    tom                     DATE                     NOT NULL,
    kilde                   VARCHAR                  NOT NULL,
    vilkår                  VARCHAR                  NOT NULL,
    detaljer                VARCHAR                  NOT NULL,
    typeSaksopplysning      VARCHAR                  NOT NULL,
    saksbehandler           VARCHAR                  NULL,
    opprettet               TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE vurdering
(
    id                      VARCHAR                  PRIMARY KEY,
    behandlingId            VARCHAR                  NULL REFERENCES behandling (id),
    vedtakId                VARCHAR                  NULL REFERENCES vedtak (id),
    fom                     DATE                     NOT NULL,
    tom                     DATE                     NOT NULL,
    kilde                   VARCHAR                  NOT NULL,
    vilkår                  VARCHAR                  NOT NULL,
    detaljer                VARCHAR                  NOT NULL,
    utfall                  VARCHAR                  NOT NULL,
    opprettet               TIMESTAMP WITH TIME ZONE NOT NULL,
    grunnlagId              VARCHAR                  NULL
);

create table attestering
(
    id                      VARCHAR PRIMARY KEY,
    behandling_id           VARCHAR                  NULL REFERENCES behandling (id),
    svar                    VARCHAR                  NOT NULL,
    begrunnelse             VARCHAR                  NULL,
    beslutter               VARCHAR                  NOT NULL,
    tidspunkt               TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE sak_personopplysninger_søker
(
    id                       VARCHAR PRIMARY KEY,
    sakId                    VARCHAR                  NOT NULL REFERENCES sak (id),
    ident                    VARCHAR                  NOT NULL,
    fødselsdato              DATE                     NOT NULL,
    fornavn                  VARCHAR                  NOT NULL,
    mellomnavn               VARCHAR                  NULL,
    etternavn                VARCHAR                  NOT NULL,
    fortrolig                BOOLEAN                  NOT NULL,
    strengt_fortrolig        BOOLEAN                  NOT NULL,
    strengt_fortrolig_utland BOOLEAN                  NOT NULL,
    skjermet                 BOOLEAN                  NULL,
    kommune                  VARCHAR                  NULL,
    bydel                    VARCHAR                  NULL,
    tidsstempel_hos_oss      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE sak_personopplysninger_barn_med_ident
(
    id                       VARCHAR PRIMARY KEY,
    sakId                    VARCHAR                  NOT NULL REFERENCES sak (id),
    ident                    VARCHAR                  NOT NULL,
    fødselsdato              DATE                     NOT NULL,
    fornavn                  VARCHAR                  NOT NULL,
    mellomnavn               VARCHAR                  NULL,
    etternavn                VARCHAR                  NOT NULL,
    fortrolig                BOOLEAN                  NOT NULL,
    strengt_fortrolig        BOOLEAN                  NOT NULL,
    strengt_fortrolig_utland BOOLEAN                  NOT NULL,
    oppholdsland             VARCHAR                  NULL,
    skjermet                 BOOLEAN                  NULL,
    tidsstempel_hos_oss      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE sak_personopplysninger_barn_uten_ident
(
    id                  VARCHAR PRIMARY KEY,
    sakId               VARCHAR                  NOT NULL REFERENCES sak (id),
    fødselsdato         DATE                     NULL,
    fornavn             VARCHAR                  NULL,
    mellomnavn          VARCHAR                  NULL,
    etternavn           VARCHAR                  NULL,
    tidsstempel_hos_oss TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE innsending
(
    id                      VARCHAR                  PRIMARY KEY,
    journalpost_id          VARCHAR                  NOT NULL UNIQUE,
    ident                   VARCHAR                  NOT NULL,
    tiltak_fom              DATE                     NOT NULL,
    tiltak_tom              DATE                     NOT NULL,
    tilstand                VARCHAR                  NOT NULL,
    tidsstempel_tiltak_innhentet TIMESTAMP WITH TIME ZONE NULL,
    tidsstempel_personopplysninger_innhentet TIMESTAMP WITH TIME ZONE NULL,
    tidsstempel_ytelser_innhentet TIMESTAMP WITH TIME ZONE NULL,
    tidsstempel_skjerming_innhentet TIMESTAMP WITH TIME ZONE NULL,
    tidsstempel_foreldrepengervedtak_innhentet TIMESTAMP WITH TIME ZONE NULL,
    tidsstempel_uførevedtak_innhentet TIMESTAMP WITH TIME ZONE NULL,
    tidsstempel_overgangsstønadvedtak_innhentet TIMESTAMP WITH TIME ZONE NULL,
    sist_endret             TIMESTAMP WITH TIME ZONE NOT NULL,
    opprettet               TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE søker
(
    id                      VARCHAR                  PRIMARY KEY,
    ident                   VARCHAR                  NOT NULL,
    sist_endret             TIMESTAMP WITH TIME ZONE NOT NULL,
    opprettet               TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE personopplysninger
(
    id                       VARCHAR PRIMARY KEY,
    søker_id                 VARCHAR                  NOT NULL REFERENCES søker (id),
    ident                    VARCHAR                  NOT NULL,
    fødselsdato              DATE                     NOT NULL,
    fornavn                  VARCHAR                  NOT NULL,
    mellomnavn               VARCHAR                  NULL,
    etternavn                VARCHAR                  NOT NULL,
    fortrolig                BOOLEAN                  NOT NULL,
    strengt_fortrolig        BOOLEAN                  NOT NULL,
    strengt_fortrolig_utland BOOLEAN                  NOT NULL,
    skjermet                 BOOLEAN                  NULL,
    kommune                  VARCHAR                  NULL,
    bydel                    VARCHAR                  NULL,
    tidsstempel_hos_oss      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE søknad
(
    id                  VARCHAR PRIMARY KEY,
    versjon             VARCHAR                  NOT NULL,
    behandling_id       VARCHAR                  NULL REFERENCES behandling (id),
    søknad_id           VARCHAR                  NOT NULL,
    ident               VARCHAR                  NOT NULL,
    fornavn             VARCHAR                  NOT NULL,
    etternavn           VARCHAR                  NOT NULL,
    journalpost_id      VARCHAR                  NOT NULL,
    dokumentinfo_id     VARCHAR                  NOT NULL,
    filnavn             VARCHAR                  NOT NULL,
    opprettet           TIMESTAMP WITH TIME ZONE NULL,
    tidsstempel_hos_oss TIMESTAMP WITH TIME ZONE NOT NULL,
    kvp_type            VARCHAR                  NOT NULL,
    kvp_ja              BOOLEAN                  NULL,
    kvp_fom             DATE                     NULL,
    kvp_tom             DATE                     NULL,
    intro_type          VARCHAR                  NOT NULL,
    intro_ja            BOOLEAN                  NULL,
    intro_fom           DATE                     NULL,
    intro_tom           DATE                     NULL,
    institusjon_type    VARCHAR                  NOT NULL,
    institusjon_ja      BOOLEAN                  NULL,
    institusjon_fom     DATE                     NULL,
    institusjon_tom     DATE                     NULL,
    sykepenger_type            VARCHAR                  NOT NULL,
    sykepenger_ja              BOOLEAN                  NULL,
    sykepenger_fom             DATE                     NULL,
    sykepenger_tom             DATE                     NULL,
    supplerende_alder_type            VARCHAR                  NOT NULL,
    supplerende_alder_ja              BOOLEAN                  NULL,
    supplerende_alder_fom             DATE                     NULL,
    supplerende_alder_tom             DATE                     NULL,
    supplerende_flyktning_type            VARCHAR                  NOT NULL,
    supplerende_flyktning_ja              BOOLEAN                  NULL,
    supplerende_flyktning_fom             DATE                     NULL,
    supplerende_flyktning_tom             DATE                     NULL,
    jobbsjansen_type            VARCHAR                  NOT NULL,
    jobbsjansen_ja              BOOLEAN                  NULL,
    jobbsjansen_fom             DATE                     NULL,
    jobbsjansen_tom             DATE                     NULL,
    gjenlevendepensjon_type            VARCHAR                  NOT NULL,
    gjenlevendepensjon_ja              BOOLEAN                  NULL,
    gjenlevendepensjon_fom             DATE                     NULL,
    gjenlevendepensjon_tom             DATE                     NULL,
    alderspensjon_type            VARCHAR                  NOT NULL,
    alderspensjon_ja              BOOLEAN                  NULL,
    alderspensjon_fom             DATE                     NULL,
    trygd_og_pensjon_type            VARCHAR                  NOT NULL,
    trygd_og_pensjon_ja              BOOLEAN                  NULL,
    trygd_og_pensjon_fom             DATE                     NULL,
    trygd_og_pensjon_tom             DATE                     NULL,
    etterlonn_type            VARCHAR                  NOT NULL
);

CREATE TABLE søknad_barnetillegg
(
    id                VARCHAR PRIMARY KEY,
    søknad_id         VARCHAR NOT NULL REFERENCES søknad (id),
    type              VARCHAR NOT NULL,
    fodselsdato       DATE    NULL,
    fornavn           VARCHAR NULL,
    mellomnavn        VARCHAR NULL,
    etternavn         VARCHAR NULL,
    opphold_i_eos_type     VARCHAR NOT NULL
);

CREATE TABLE søknad_tiltak
(
    id                      VARCHAR PRIMARY KEY,
    søknad_id               VARCHAR NOT NULL REFERENCES søknad (id),
    ekstern_id              VARCHAR NOT NULL,
    arrangørnavn            VARCHAR NULL,
    typekode                VARCHAR NOT NULL,
    typenavn                VARCHAR NOT NULL,
    deltakelse_fom          DATE    NOT NULL,
    deltakelse_tom          DATE    NULL
);

CREATE TABLE søknad_vedlegg
(
    id              VARCHAR PRIMARY KEY,
    søknad_id       VARCHAR NOT NULL REFERENCES søknad (id),
    journalpost_id  VARCHAR NOT NULL,
    dokumentinfo_id VARCHAR NOT NULL,
    filnavn         VARCHAR NULL
);

CREATE TABLE personopplysninger_søker
(
    id                       VARCHAR PRIMARY KEY,
    innsending_id            VARCHAR                  NOT NULL REFERENCES innsending (id),
    ident                    VARCHAR                  NOT NULL,
    fødselsdato              DATE                     NOT NULL,
    fornavn                  VARCHAR                  NOT NULL,
    mellomnavn               VARCHAR                  NULL,
    etternavn                VARCHAR                  NOT NULL,
    fortrolig                BOOLEAN                  NOT NULL,
    strengt_fortrolig        BOOLEAN                  NOT NULL,
    strengt_fortrolig_utland BOOLEAN                  NOT NULL,
    skjermet                 BOOLEAN                  NULL,
    kommune                  VARCHAR                  NULL,
    bydel                    VARCHAR                  NULL,
    tidsstempel_hos_oss      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE personopplysninger_barn_med_ident
(
    id                       VARCHAR PRIMARY KEY,
    innsending_id            VARCHAR                  NOT NULL REFERENCES innsending (id),
    ident                    VARCHAR                  NOT NULL,
    fødselsdato              DATE                     NOT NULL,
    fornavn                  VARCHAR                  NOT NULL,
    mellomnavn               VARCHAR                  NULL,
    etternavn                VARCHAR                  NOT NULL,
    fortrolig                BOOLEAN                  NOT NULL,
    strengt_fortrolig        BOOLEAN                  NOT NULL,
    strengt_fortrolig_utland BOOLEAN                  NOT NULL,
    oppholdsland             VARCHAR                  NULL,
    skjermet                 BOOLEAN                  NULL,
    tidsstempel_hos_oss      TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE personopplysninger_barn_uten_ident
(
    id                  VARCHAR PRIMARY KEY,
    innsending_id       VARCHAR                  NOT NULL REFERENCES innsending (id),
    fødselsdato         DATE                     NULL,
    fornavn             VARCHAR                  NULL,
    mellomnavn          VARCHAR                  NULL,
    etternavn           VARCHAR                  NULL,
    tidsstempel_hos_oss TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE tiltak
(
    id                     VARCHAR PRIMARY KEY,
    behandling_id          VARCHAR                  NOT NULL REFERENCES behandling (id),
    ekstern_id             VARCHAR                  NOT NULL,
    gjennomføring_id       VARCHAR                  NOT NULL,
    tiltaktype_kode        VARCHAR                  NOT NULL,
    tiltaktype_navn        VARCHAR                  NOT NULL,
    arrangørnavn           VARCHAR                  NOT NULL,
    rett_på_tiltakspenger  BOOLEAN                  NOT NULL,
    deltakelse_fom         DATE                     NULL,
    deltakelse_tom         DATE                     NULL,
    deltakelse_prosent     FLOAT                    NULL,
    deltakelse_status      VARCHAR                  NOT NULL,
    rett_til_å_søke        BOOLEAN                  NOT NULL,
    kilde                  VARCHAR                  NOT NULL,
    tidsstempel_kilde      TIMESTAMP WITH TIME ZONE NOT NULL,
    tidsstempel_hos_oss    TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE ytelsesak
(
    id                    VARCHAR PRIMARY KEY,
    innsending_id         VARCHAR                  NOT NULL REFERENCES innsending (id),
    fom_gyldighetsperiode TIMESTAMP WITH TIME ZONE NOT NULL,
    tom_gyldighetsperiode TIMESTAMP WITH TIME ZONE NULL,
    dato_krav_mottatt     DATE                     NULL,
    data_krav_mottatt     VARCHAR                  NULL,
    fagsystem_sak_id      VARCHAR                  NULL,
    status                VARCHAR                  NULL,
    ytelsestype           VARCHAR                  NULL,
    antall_dager_igjen    INT                      NULL,
    antall_uker_igjen     INT                      NULL,
    tidsstempel_hos_oss   TIMESTAMP WITH TIME ZONE NOT NULL
);

create table ytelsevedtak
(
    id                     VARCHAR PRIMARY KEY,
    ytelsesak_id           VARCHAR NOT NULL REFERENCES ytelsesak (id),
    beslutnings_dato       DATE    NULL,
    periodetype_for_ytelse VARCHAR NULL,
    vedtaksperiode_fom     DATE    NULL,
    vedtaksperiode_tom     DATE    NULL,
    vedtaks_type           VARCHAR NULL,
    status                 VARCHAR NULL
);

create table aktivitet
(
    id               VARCHAR PRIMARY KEY,
    innsending_id    VARCHAR                  NOT NULL REFERENCES innsending (id),
    type             VARCHAR                  NULL,
    alvorlighetsgrad INT                      NOT NULL,
    label            CHAR(1)                  NOT NULL,
    melding          VARCHAR                  NOT NULL,
    tidsstempel      TIMESTAMP WITH TIME ZONE NOT NULL,
    detaljer         JSONB                    NULL,
    kontekster       JSONB                    NOT NULL
);

CREATE TABLE foreldrepenger_vedtak
(
    id                   VARCHAR PRIMARY KEY,
    innsending_id        VARCHAR                  NOT NULL REFERENCES innsending (id),
    version              VARCHAR                  NOT NULL,
    aktør                VARCHAR                  NOT NULL,
    vedtatt_tidspunkt    TIMESTAMP WITH TIME ZONE NOT NULL,
    ytelse               VARCHAR                  NOT NULL,
    saksnummer           VARCHAR                  NULL,
    vedtakReferanse      VARCHAR                  NOT NULL,
    ytelseStatus         VARCHAR                  NOT NULL,
    kildesystem          VARCHAR                  NOT NULL,
    fra                  DATE                     NOT NULL,
    til                  DATE                     NOT NULL,
    tilleggsopplysninger VARCHAR                  NULL,
    innhentet TIMESTAMP WITH TIME ZONE NULL,
    tidsstempel_hos_oss  TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE foreldrepenger_anvisning
(
    id                       VARCHAR PRIMARY KEY,
    foreldrepenger_vedtak_id VARCHAR NOT NULL REFERENCES foreldrepenger_vedtak (id),
    fra                      DATE    NOT NULL,
    til                      DATE    NOT NULL,
    beløp                    decimal null,
    dagsats                  decimal null,
    utbetalingsgrad          decimal null
);

CREATE TABLE uføre_vedtak
(
    id                  VARCHAR PRIMARY KEY,
    innsending_id       VARCHAR                  NOT NULL REFERENCES innsending (id),
    har_uforegrad       BOOLEAN                  NOT NULL,
    dato_ufor           DATE                     NULL,
    virk_dato           DATE                     NULL,
    innhentet           TIMESTAMP WITH TIME ZONE NOT NULL,
    tidsstempel_hos_oss TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE overgangsstønad_vedtak
(
    id                  VARCHAR PRIMARY KEY,
    innsending_id       VARCHAR                  NOT NULL REFERENCES innsending (id),
    fom                 DATE                     NOT NULL,
    tom                 DATE                     NOT NULL,
    datakilde           VARCHAR                  NOT NULL,
    innhentet           TIMESTAMP WITH TIME ZONE NOT NULL,
    tidsstempel_hos_oss TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE stønadsdager_tiltak
(
    id                  VARCHAR PRIMARY KEY,
    antall_dager        NUMERIC                  NOT NULL,
    fom                 DATE                     NOT NULL,
    tom                 DATE                     NOT NULL,
    datakilde           VARCHAR                  NOT NULL,
    tidsstempel_kilde   TIMESTAMP WITH TIME ZONE NOT NULL,
    tidsstempel_hos_oss TIMESTAMP WITH TIME ZONE NOT NULL,
    tiltak_id           VARCHAR                  NOT NULL REFERENCES tiltak (id),
    behandling_id       VARCHAR                  NOT NULL REFERENCES behandling (id),
    avklart_tidspunkt   TIMESTAMP WITH TIME ZONE NULL,
    saksbehandler       VARCHAR                  NULL
);

CREATE TABLE kravdato_saksopplysning
(
    id                  VARCHAR PRIMARY KEY,
    kravdato            TIMESTAMP WITH TIME ZONE NOT NULL,
    behandling_id       VARCHAR                  NOT NULL REFERENCES behandling (id),
    avklart_tidspunkt   TIMESTAMP WITH TIME ZONE NULL,
    saksbehandler       VARCHAR                  NULL,
    datakilde           VARCHAR                  NOT NULL
);

create sequence sak_løpenr start 1001;
