ALTER TABLE innsending
    ADD COLUMN IF NOT EXISTS
        tidsstempel_foreldrepengervedtak_innhentet TIMESTAMP WITH TIME ZONE NULL
            DEFAULT null;


CREATE TABLE foreldrepenger_vedtak
(
    id                   VARCHAR PRIMARY KEY,
    innsending_id        VARCHAR                  NOT NULL REFERENCES innsending (id),
    version              VARCHAR                  NOT NULL,
    aktør                VARCHAR                  NOT NULL,
    vedtatt_tidspunkt    TIMESTAMP WITH TIME ZONE NOT NULL,
    ytelse               VARCHAR                  NOT NULL,
    saksnummer           VARCHAR                  NOT NULL,
    vedtakReferanse      VARCHAR                  NOT NULL,
    ytelseStatus         VARCHAR                  NOT NULL,
    kildesystem          VARCHAR                  NOT NULL,
    fra                  DATE                     NOT NULL,
    til                  DATE                     NOT NULL,
    tilleggsopplysninger VARCHAR                  NULL,
    tidsstempel_hos_oss  TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE foreldrepenger_anvisning
(
    id                       VARCHAR PRIMARY KEY,
    foreldrepenger_vedtak_id VARCHAR NOT NULL REFERENCES foreldrepenger_vedtak (id),
    fra                      DATE    NOT NULL,
    til                      DATE    NOT NULL,
    beløp                    decimal null,
    dagsats                  decimal null,
    utbetalingsgrad          decimal null
);
