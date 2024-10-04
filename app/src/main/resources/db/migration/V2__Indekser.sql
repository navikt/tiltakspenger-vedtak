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

CREATE INDEX tiltak_behandling ON tiltak (behandling_id);

CREATE INDEX idx_utfylt_meldekort_sakid ON meldekort (sakId);

CREATE INDEX idx_utfylt_meldekort_rammevedtakid ON meldekort (rammevedtakId);
