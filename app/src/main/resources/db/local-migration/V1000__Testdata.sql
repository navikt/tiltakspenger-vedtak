CREATE TABLE DUMMY
(
    id          VARCHAR PRIMARY KEY,
    ident       VARCHAR                  NOT NULL UNIQUE,
    tilstand    VARCHAR                  NOT NULL,
    sist_endret TIMESTAMP WITH TIME ZONE NOT NULL,
    opprettet   TIMESTAMP WITH TIME ZONE NOT NULL
);
