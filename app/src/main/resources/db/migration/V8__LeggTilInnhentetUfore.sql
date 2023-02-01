ALTER TABLE innsending
    ADD COLUMN IF NOT EXISTS
        tidsstempel_uførevedtak_innhentet TIMESTAMP WITH TIME ZONE NULL
            DEFAULT null;


CREATE TABLE uføre_vedtak
(
    id                  VARCHAR PRIMARY KEY,
    innsending_id       VARCHAR                  NOT NULL REFERENCES innsending (id),
    har_uforegrad       BOOLEAN                  NOT NULL,
    dato_ufor           DATE                     NULL,
    virk_dato           DATE                     NULL,
    innhentet           TIMESTAMP WITH TIME ZONE NOT NULL,
    tidsstempel_hos_oss TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX uførevedtak_innsending
    ON uføre_vedtak
        (
         innsending_id
            );

CREATE INDEX foreldrepengervedtak_innsending
    ON foreldrepenger_vedtak
        (
         innsending_id
            );

CREATE INDEX foreldrepengeranvisning_innsending
    ON foreldrepenger_anvisning
        (
         foreldrepenger_vedtak_id
            );
