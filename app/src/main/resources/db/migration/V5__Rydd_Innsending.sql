DROP INDEX ytelsesak_innsending;
DROP INDEX ytelsevedtak_ytelsesak;
DROP INDEX uførevedtak_innsending;
DROP INDEX foreldrepengervedtak_innsending;
DROP INDEX foreldrepengeranvisning_innsending;
DROP INDEX overgangsstønad_vedtak_innsending;

DROP TABLE ytelsevedtak;
DROP TABLE ytelsesak;
DROP TABLE foreldrepenger_anvisning;
DROP TABLE foreldrepenger_vedtak;
DROP TABLE uføre_vedtak;
DROP TABLE overgangsstønad_vedtak;

-- TODO: Hva gjør RESTRICT?
ALTER TABLE innsending DROP column tidsstempel_ytelser_innhentet RESTRICT;
ALTER TABLE innsending DROP column tidsstempel_foreldrepengervedtak_innhentet RESTRICT;
ALTER TABLE innsending DROP column tidsstempel_uførevedtak_innhentet RESTRICT;
ALTER TABLE innsending DROP column tidsstempel_overgangsstønadvedtak_innhentet RESTRICT;
