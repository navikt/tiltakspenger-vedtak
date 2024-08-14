create TABLE statistikk_stønad
(
    id                      VARCHAR                  PRIMARY KEY,

    -- felter for sak
    bruker_id               VARCHAR                  NULL, -- uuid for søker?
    sak_id                  VARCHAR                  NULL,
    resultat                VARCHAR                  NULL, -- Innvilget, Avslått, Avvist, Henlagt, Delvis innvilget
    sak_dato                DATE                     NULL, -- Hvilken dato er dette?
    gyldig_fra_dato         DATE                     NULL,
    gyldig_til_dato         DATE                     NULL,

    -- felter for ytelse
    ytelse                  VARCHAR                  NULL,  -- type TP-stønad ??

    -- felter for søknad
    søknad_id               VARCHAR                  NULL,
    opplysning              VARCHAR                  NULL, -- Relevante opplysninger ???
    søknad_dato             DATE                     NULL,
    gyldig_fra_dato_søknad  DATE                     NULL, -- Hvilken dato er dette?
    gyldig_til_dato_søknad  DATE                     NULL, -- Hvilken dato er dette?

    -- felter for vedtak
    vedtak_id               VARCHAR                  NULL,
    type                    VARCHAR                  NULL, -- Søknad, Revurdering, Gjenopptak, Ny rettighet, Klage, Anke
    vedtak_dato             DATE                     NULL,
    fom                     DATE                     NULL,
    tom                     DATE                     NULL,

    -- nav enhet
    oppfølging_enhet_kode   VARCHAR                  NULL,
    oppfølging_enhet_navn   VARCHAR                  NULL,
    beslutning_enhet_kode   VARCHAR                  NULL,
    beslutning_enhet_navn   VARCHAR                  NULL,
    tilhørighet_enhet_kode  VARCHAR                  NULL,
    tilhørighet_enhet_navn  VARCHAR                  NULL,

    -- felter for vilkår
    vilkår_id               VARCHAR                  NULL,
    vilkår_type             VARCHAR                  NULL,
    vilkår_status           VARCHAR                  NULL, -- Godkjent, Avslått
    lovparagraf             VARCHAR                  NULL,
    beskrivelse             VARCHAR                  NULL,
    gyldig_fra_dato_vilkår  DATE                     NULL,
    gyldig_til_dato_vilkår  DATE                     NULL,

    -- felter for postering
    postering_id            VARCHAR                  NULL,
    beløp                   DECIMAL                  NULL,
    beløp_beskrivelse       VARCHAR                  NULL,
    aarsak                  VARCHAR                  NULL,
    postering_dato          DATE                     NULL,
    gyldig_fra_dato_postering DATE                   NULL,
    gyldig_til_dato_postering DATE                   NULL,

    -- felter for tiltak
    tiltak_id               VARCHAR                  NULL,
    tiltak_type             VARCHAR                  NULL,
    tiltak_beskrivelse      VARCHAR                  NULL,
    fagsystem               VARCHAR                  NULL,
    tiltak_dato             DATE                     NULL,
    gyldig_fra_dato_tiltak   DATE                    NULL,
    gyldig_til_dato_tiltak   DATE                    NULL,

    sist_endret             TIMESTAMP WITH TIME ZONE NULL,
    opprettet               TIMESTAMP WITH TIME ZONE NULL
);
