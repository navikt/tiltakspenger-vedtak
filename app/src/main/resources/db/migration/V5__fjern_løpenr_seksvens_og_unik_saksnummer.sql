ALTER TABLE sak
ADD CONSTRAINT unique_saksnummer UNIQUE (saksnummer);

DROP SEQUENCE IF EXISTS sak_løpenr;