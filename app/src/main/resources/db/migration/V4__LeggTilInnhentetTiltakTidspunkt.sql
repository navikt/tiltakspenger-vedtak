ALTER TABLE innsending
    ADD COLUMN IF NOT EXISTS
    tidsstempel_tiltak_innhentet TIMESTAMP WITH TIME ZONE NULL
            DEFAULT null;

ALTER TABLE innsending
    ADD COLUMN IF NOT EXISTS
    tidsstempel_personopplysninger_innhentet TIMESTAMP WITH TIME ZONE NULL
    DEFAULT null;

ALTER TABLE innsending
    ADD COLUMN IF NOT EXISTS
    tidsstempel_ytelser_innhentet TIMESTAMP WITH TIME ZONE NULL
    DEFAULT null;

UPDATE innsending SET tidsstempel_tiltak_innhentet = now() WHERE id IN
(
    SELECT id FROM innsending WHERE tilstand in ('AvventerYtelser', 'InnsendingFerdigstilt')
);

UPDATE innsending SET tidsstempel_personopplysninger_innhentet = now() WHERE id IN
(
 SELECT id FROM innsending WHERE tilstand in ('AvventerTiltak', 'AvventerYtelser', 'InnsendingFerdigstilt')
);

UPDATE innsending SET tidsstempel_ytelser_innhentet = now() WHERE id IN
(
 SELECT id FROM innsending WHERE tilstand in ('InnsendingFerdigstilt')
);
