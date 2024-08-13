CREATE INDEX sak_behandling
    ON behandling
        (
         sakId
            );


CREATE INDEX saks_ident
    ON sak
        (
         ident
            );

CREATE INDEX sakPersonopplysningerSøker
    ON sak_personopplysninger_søker
        (
         sakId
            );

CREATE INDEX sakPersonopplysningerBarnMedIdent
    ON sak_personopplysninger_barn_med_ident
        (
         sakId
            );

CREATE INDEX sakPersonopplysningerBarnUtenIdent
    ON sak_personopplysninger_barn_uten_ident
        (
         sakId
            );

CREATE INDEX søknad_ident
    ON søknad
        (
         ident
            );

CREATE INDEX søknad_behandling
    ON søknad
        (
         behandling_id
            );

CREATE INDEX søknadTiltak_søknad
    ON søknad_tiltak
        (
         søknad_id
            );

CREATE INDEX søknadBarnetillegg_søknad
    ON søknad_barnetillegg
        (
         søknad_id
            );

CREATE INDEX søknadVedlegg_søknad
    ON søknad_vedlegg
        (
         søknad_id
            );

CREATE INDEX tiltak_behandling
    ON tiltak
        (
         behandling_id
            );
