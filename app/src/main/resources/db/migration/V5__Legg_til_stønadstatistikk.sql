create TABLE statistikk_stønad
(
    id                      VARCHAR                  PRIMARY KEY,

    -- felter for sak
    bruker_id               VARCHAR                  NOT NULL, -- uuid for søker?
    sak_id                  VARCHAR                  NOT NULL,
    resultat                VARCHAR                  NOT NULL, -- Innvilget, Avslått, Avvist, Henlagt, Delvis innvilget
    sak_dato                DATE                     NOT NULL, -- Hvilken dato er dette?
    gyldig_fra_dato         DATE                     NOT NULL,
    gyldig_til_dato         DATE                     NOT NULL,

    -- felter for ytelse
    ytelse                  VARCHAR                  NOT NULL,  -- type TP-stønad ??

    -- felter for søknad
    søknad_id               VARCHAR                  NOT NULL,
    opplysning              VARCHAR                  NOT NULL, -- Relevante opplysninger ???
    søknad_dato             DATE                     NOT NULL,
    gyldig_fra_dato_søknad  DATE                     NOT NULL, -- Hvilken dato er dette?
    gyldig_til_dato_søknad  DATE                     NOT NULL, -- Hvilken dato er dette?

    -- felter for vedtak
    vedtak_id               VARCHAR                  NOT NULL,
    type                    VARCHAR                  NOT NULL, -- Søknad, Revurdering, Gjenopptak, Ny rettighet, Klage, Anke
    vedtak_dato             DATE                     NOT NULL,
    fom                     DATE                     NOT NULL,
    tom                     DATE                     NOT NULL,

    -- nav enhet
    oppfølging_enhet_kode   VARCHAR                  NOT NULL,
    oppfølging_enhet_navn   VARCHAR                  NOT NULL,
    beslutning_enhet_kode   VARCHAR                  NOT NULL,
    beslutning_enhet_navn   VARCHAR                  NOT NULL,
    tilhørighet_enhet_kode  VARCHAR                  NOT NULL,
    tilhørighet_enhet_navn  VARCHAR                  NOT NULL,

    -- felter for vilkår
    vilkår_id               VARCHAR                  NOT NULL,
    vilkår_type             VARCHAR                  NOT NULL,
    vilkår_status           VARCHAR                  NOT NULL, -- Godkjent, Avslått
    lovparagraf             VARCHAR                  NOT NULL,
    beskrivelse             VARCHAR                  NOT NULL,
    gyldig_fra_dato_vilkår  DATE                     NOT NULL,
    gyldig_til_dato_vilkår  DATE                     NOT NULL,

    -- felter for postering
    postering_id            VARCHAR                  NOT NULL,
    beløp                   DECIMAL                  NOT NULL,
    beløp_beskrivelse       VARCHAR                  NOT NULL,
    aarsak                  VARCHAR                  NOT NULL,
    postering_dato          DATE                     NOT NULL,
    gyldig_fra_dato_postering DATE                    NOT NULL,
    gyldig_til_dato_postering DATE                    NOT NULL,

    -- felter for tiltak
    tiltak_id               VARCHAR                  NOT NULL,
    tiltak_type             VARCHAR                  NOT NULL,
    tiltak_beskrivelse      VARCHAR                  NOT NULL,
    fagsystem               VARCHAR                  NOT NULL,
    tiltak_dato             DATE                     NOT NULL,
    gyldig_fra_dato_tiltak   DATE                    NOT NULL,
    gyldig_til_dato_tiltak   DATE                    NOT NULL,

    sist_endret             TIMESTAMP WITH TIME ZONE NOT NULL,
    opprettet               TIMESTAMP WITH TIME ZONE NOT NULL
);