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
