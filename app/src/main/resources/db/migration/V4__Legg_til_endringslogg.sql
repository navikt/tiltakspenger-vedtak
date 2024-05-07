CREATE TABLE endring
(
    id               VARCHAR PRIMARY KEY,
    sak_id           VARCHAR                  NOT NULL REFERENCES sak (id),
    behandling_id    VARCHAR                  NULL REFERENCES behandling (id),
    type             VARCHAR                  NULL,
    alvorlighetsgrad INT                      NOT NULL,
    label            CHAR(1)                  NOT NULL,
    brukernavn       VARCHAR                  NULL,
    melding          VARCHAR                  NOT NULL,
    tidsstempel      TIMESTAMP WITH TIME ZONE NOT NULL,
    detaljer         JSONB                    NULL,
    kontekster       JSONB                    NOT NULL
);

CREATE INDEX endring_sak
    ON endring
        (
         sak_id
            );

CREATE INDEX endring_behandling
    ON endring
        (
         behandling_id
            );
