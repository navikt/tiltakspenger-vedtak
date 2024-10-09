DROP TABLE IF EXISTS stønadsdager_tiltak;
DROP TABLE IF EXISTS tiltak;
DROP INDEX IF EXISTS tiltak_behandling;

ALTER TABLE søknad_tiltak RENAME TO søknadstiltak;