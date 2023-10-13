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

CREATE INDEX vurdering_behandling
    ON vurdering
        (
         behandlingId
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

CREATE INDEX aktivitet_innsending
    ON aktivitet
        (
         innsending_id
            );

CREATE INDEX innsending_ident
    ON innsending
        (
         ident
            );

CREATE INDEX innsending_tilstand
    ON innsending
        (
         tilstand
            );

CREATE INDEX innsending_sistendret
    ON innsending
        (
         sist_endret
            );

CREATE INDEX personopplysninger_søkerid
    ON personopplysninger
        (
         søker_id
            );

CREATE INDEX personopplysningerBarnMedIdent_innsending
    ON personopplysninger_barn_med_ident
        (
         innsending_id
            );

CREATE INDEX personopplysningerBarnUtenIdent_innsending
    ON personopplysninger_barn_uten_ident
        (
         innsending_id
            );

CREATE INDEX personopplysningerSøker_innsending
    ON personopplysninger_søker
        (
         innsending_id
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

CREATE INDEX søknad_innsending
    ON søknad
        (
         innsending_id
            );

CREATE INDEX søknad_behandling
    ON søknad
        (
         behandling_id
            );

CREATE INDEX søknadArenatiltak_søknad
    ON søknad_arenatiltak
        (
         søknad_id
            );

CREATE INDEX søknadBrukertiltak_søknad
    ON søknad_brukertiltak
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

CREATE INDEX tiltaksaktivitet_innsending
    ON tiltaksaktivitet
        (
         innsending_id
            );

CREATE INDEX ytelsesak_innsending
    ON ytelsesak
        (
         innsending_id
            );

CREATE INDEX ytelsevedtak_ytelsesak
    ON ytelsevedtak
        (
         ytelsesak_id
            );

CREATE INDEX uførevedtak_innsending
    ON uføre_vedtak
        (
         innsending_id
            );

CREATE INDEX foreldrepengervedtak_innsending
    ON foreldrepenger_vedtak
        (
         innsending_id
            );

CREATE INDEX foreldrepengeranvisning_innsending
    ON foreldrepenger_anvisning
        (
         foreldrepenger_vedtak_id
            );

CREATE INDEX innsending_journalpost_id
    ON innsending
        (
         journalpost_id
            );

CREATE INDEX søknad_søknad_id
    ON søknad
        (
         søknad_id
            );

CREATE INDEX overgangsstønad_vedtak_innsending
    ON overgangsstønad_vedtak
        (
         innsending_id
            );
