ALTER TABLE innsending
    ADD COLUMN IF NOT EXISTS
    tidsstempel_skjerming_innhentet TIMESTAMP WITH TIME ZONE NULL
    DEFAULT null;


UPDATE innsending SET tidsstempel_skjerming_innhentet = now() WHERE id IN
(
    SELECT id FROM innsending WHERE tilstand in ('AvventerTiltak', 'AvventerYtelser', 'InnsendingFerdigstilt')
);

UPDATE innsending SET tidsstempel_personopplysninger_innhentet = now() WHERE id IN
(
 SELECT id FROM innsending WHERE tilstand in ('AvventerSkjermingdata', 'AvventerTiltak', 'AvventerYtelser', 'InnsendingFerdigstilt')
);
