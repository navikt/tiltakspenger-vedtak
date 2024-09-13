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
    saksnummer              VARCHAR                  NOT NULL UNIQUE,
    sist_endret             TIMESTAMP WITH TIME ZONE NOT NULL,
    opprettet               TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE behandling
(
    id                      VARCHAR                  PRIMARY KEY,
    sakId                   VARCHAR                  NOT NULL REFERENCES sak (id),
    fom                     DATE                     NOT NULL,
    tom                     DATE                     NOT NULL,
    status                  VARCHAR                  NOT NULL,
    saksbehandler           VARCHAR                  NULL,
    beslutter               VARCHAR                  NULL,
    iverksattTidspunkt      TIMESTAMP WITH TIME ZONE NULL,
    sist_endret             TIMESTAMP WITH TIME ZONE NOT NULL,
    opprettet               TIMESTAMP WITH TIME ZONE NOT NULL,
    vilkårssett             JSONB                    NOT NULL,
    stønadsdager            JSONB                    NOT NULL,
    attesteringer           JSONB                    NULL
);

CREATE TABLE rammevedtak
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

CREATE TABLE søknad
(
    id                  VARCHAR                  PRIMARY KEY,
    versjon             VARCHAR                  NOT NULL,
    sak_id              VARCHAR                  NULL REFERENCES sak (id),
    behandling_id       VARCHAR                  NULL REFERENCES behandling (id),
    ident               VARCHAR                  NOT NULL,
    fornavn             VARCHAR                  NOT NULL,
    etternavn           VARCHAR                  NOT NULL,
    journalpost_id      VARCHAR                  NOT NULL,
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
    etterlonn_type            VARCHAR                  NOT NULL,
    vedlegg                   INT                      NOT NULL
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
    kilde                  VARCHAR                  NOT NULL,
    tidsstempel_kilde      TIMESTAMP WITH TIME ZONE NOT NULL,
    tidsstempel_hos_oss    TIMESTAMP WITH TIME ZONE NOT NULL
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

create table meldekort
(
    id                   varchar primary key,
    sakId                varchar not null REFERENCES sak(id),
    rammevedtakId        varchar not null REFERENCES rammevedtak(id),
    forrigeMeldekortId   varchar null references meldekort(id),
    fraOgMed             date not null,
    tilOgMed             date not null,
    meldekortdager       jsonb not null,
    saksbehandler        varchar null,
    beslutter            varchar null,
    status               varchar not null
);

create table utbetalingsvedtak
(
    id                   varchar primary key,
    sakId                varchar not null references sak(id),
    rammevedtakId        varchar not null references rammevedtak(id),
    brukerNavkontor      varchar not null,
    vedtakstidspunkt     timestamp not null,
    saksbehandler        varchar not null,
    beslutter            varchar not null,
    forrigeVedtakId      varchar null references utbetalingsvedtak(id),
    meldekortId          varchar not null references meldekort(id),
    utbetalingsperiode   jsonb not null,
    sendt_til_utbetaling boolean not null default false,
    sendt_til_dokument   boolean not null default false,
    utbetaling_metadata   jsonb null
);

create table statistikk_utbetaling
(
    id                   varchar primary key,
    sak_id               varchar not null,
    saksnummer           varchar not null,
    beløp                int not null,
    beløp_beskrivelse    varchar not null,
    årsak                varchar not null,
    posteringsdato       date not null,
    gyldig_fra_dato      date not null,
    gyldig_til_dato      date not null
);

create table statistikk_stønad
(
    id                      varchar                  primary key,
    bruker_id               varchar                  null,
    sak_id                  varchar                  null,
    saksnummer              varchar                  null,
    resultat                varchar                  null,
    sak_dato                date                     null,
    gyldig_fra_dato         date                     null,
    gyldig_til_dato         date                     null,
    ytelse                  varchar                  null,
    søknad_id               varchar                  null,
    opplysning              varchar                  null,
    søknad_dato             date                     null,
    gyldig_fra_dato_søknad  date                     null,
    gyldig_til_dato_søknad  date                     null,
    vedtak_id               varchar                  null,
    type                    varchar                  null,
    vedtak_dato             date                     null,
    fom                     date                     null,
    tom                     date                     null,
    oppfølging_enhet_kode   varchar                  null,
    oppfølging_enhet_navn   varchar                  null,
    beslutning_enhet_kode   varchar                  null,
    beslutning_enhet_navn   varchar                  null,
    tilhørighet_enhet_kode  varchar                  null,
    tilhørighet_enhet_navn  varchar                  null,
    vilkår_id               varchar                  null,
    vilkår_type             varchar                  null,
    vilkår_status           varchar                  null,
    lovparagraf             varchar                  null,
    beskrivelse             varchar                  null,
    gyldig_fra_dato_vilkår  date                     null,
    gyldig_til_dato_vilkår  date                     null,
    tiltak_id               varchar                  null,
    tiltak_type             varchar                  null,
    tiltak_beskrivelse      varchar                  null,
    fagsystem               varchar                  null,
    tiltak_dato             date                     null,
    gyldig_fra_dato_tiltak  date                     null,
    gyldig_til_dato_tiltak  date                     null,
    sist_endret             timestamp with time zone null,
    opprettet               timestamp with time zone null
);

create table statistikk_sak
(
    id                     serial                   primary key,
    sak_id                 varchar                  null,
    saksnummer             varchar                  null,
    behandlingid           varchar                  null,
    relatertbehandlingid   varchar                  null,
    ident                  varchar                  null,
    mottatt_tidspunkt      timestamp with time zone null,
    registrerttidspunkt    timestamp with time zone null,
    ferdigbehandlettidspunkt timestamp with time zone null,
    vedtaktidspunkt        timestamp with time zone null,
    utbetalttidspunkt      timestamp with time zone null,
    endrettidspunkt        timestamp with time zone null,
    søknadsformat          varchar                  null,
    forventetoppstarttidspunkt timestamp with time zone null,
    teknisktidspunkt       timestamp with time zone null,
    sakytelse              varchar                  null,
    sakutland              boolean                  null,
    behandlingtype         varchar                  null,
    behandlingstatus       varchar                  null,
    behandlingresultat     varchar                  null,
    resultatbegrunnelse    varchar                  null,
    behandlingmetode       varchar                  null,
    opprettetav            varchar                  null,
    saksbehandler          varchar                  null,
    ansvarligbeslutter     varchar                  null,
    ansvarligenhet         varchar                  null,
    tilbakekrevingsbeløp   decimal                  null,
    funksjonellperiodefom  date                     null,
    funksjonellperiodetom  date                     null,
    hendelse               varchar                  null,
    avsender               varchar                  null,
    versjon                varchar                  null
);

create table statistikk_sak_vilkår
(
    statistikk_sak_id      int                      not null references statistikk_sak (id),
    vilkår                 varchar                  null,
    beskrivelse            varchar                  null,
    resultat               varchar                  null
);
