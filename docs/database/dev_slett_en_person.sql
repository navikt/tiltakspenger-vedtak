WITH
sakz AS (DELETE FROM sak WHERE ident = :replace-with-fnr RETURNING id),
behandlingz AS (DELETE FROM behandling WHERE sakId IN (SELECT id FROM sakz) returning id),
søknadz AS (DELETE FROM søknad WHERE sak_id IN (SELECT id FROM sakz) OR behandling_id in (select id from behandlingz) returning id),
søknad_barnetilleggz AS (DELETE FROM søknad_barnetillegg WHERE søknad_id IN (SELECT id FROM søknadz)),
søknadstiltakz AS (DELETE FROM søknadstiltak WHERE søknad_id IN (SELECT id FROM søknadz)),
rammevedtakz AS (DELETE FROM rammevedtak WHERE sak_id IN (SELECT id FROM sakz)),
meldekortz AS (DELETE FROM meldekort WHERE sakId IN (SELECT id FROM sakz)),
utbetalingsvedtakz AS (DELETE FROM utbetalingsvedtak WHERE sakId IN (SELECT id FROM sakz)),
statistikk_utbetalingz AS (DELETE FROM statistikk_utbetaling WHERE sak_id IN (SELECT id FROM sakz)),
statistikk_stønadz AS (DELETE FROM statistikk_stønad WHERE sak_id IN (SELECT id FROM sakz)),
statistikk_sakz AS (DELETE FROM statistikk_sak WHERE sak_id IN (SELECT id FROM sakz) returning id),
statistikk_sak_vilkårz AS (DELETE FROM statistikk_sak_vilkår WHERE statistikk_sak_id IN (SELECT id FROM statistikk_sakz))
SELECT id FROM sakz;