ALTER TABLE innsending
    ADD COLUMN IF NOT EXISTS
    tidsstempel_overgangsstønadvedtak_innhentet TIMESTAMP WITH TIME ZONE NULL
    DEFAULT null;


CREATE TABLE overgangsstønad_vedtak
(
    id                  VARCHAR PRIMARY KEY,
    innsending_id       VARCHAR                  NOT NULL REFERENCES innsending (id),
    fom                 DATE                     NOT NULL,
    tom                 DATE                     NOT NULL,
    datakilde           VARCHAR                  NOT NULL,
    innhentet           TIMESTAMP WITH TIME ZONE NOT NULL,
    tidsstempel_hos_oss TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX overgangsstønad_vedtak_innsending
    ON overgangsstønad_vedtak
        (
         innsending_id
            );
