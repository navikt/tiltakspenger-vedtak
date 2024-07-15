CREATE INDEX sak_behandling
    ON behandling
        (
         sakId
            );

CREATE INDEX saksopplysning_behandling
    ON saksopplysning
        (
         behandlingId
            );

CREATE INDEX saksopplysning_vedtak
    ON saksopplysning
        (
         vedtakId
            );

CREATE INDEX vurdering_behandling
    ON vurdering
        (
         behandlingId
            );

CREATE INDEX vurdering_vedtak
    ON vurdering
        (
         vedtakId
            );

CREATE INDEX utfallsperiode_vedtak
    ON utfallsperiode
        (
         vedtak_id
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

CREATE INDEX søker_ident
    ON søker
        (
         ident
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

CREATE INDEX søknad_søknad_id
    ON søknad
        (
         søknad_id
            );
