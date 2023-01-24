ALTER TABLE innsending
    ADD COLUMN IF NOT EXISTS
    tidsstempel_tiltak_innhentet TIMESTAMP WITH TIME ZONE NULL
            DEFAULT null;

UPDATE innsending SET tidsstempel_tiltak_innhentet = now() WHERE id IN
(
    SELECT id FROM innsending WHERE tilstand in ('AvventerYtelser', 'InnsendingFerdigstilt')
)
