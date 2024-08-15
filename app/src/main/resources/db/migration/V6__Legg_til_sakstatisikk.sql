create table statistikk_sak
(
    id                     serial                   primary key,
    sak_id                 varchar                  not null,
    saksnummer             varchar                  not null,
    behandlingid           varchar                  not null,
    relatertbehandlingid   varchar                  null,
    ident                  varchar                  not null,
    mottatttidspunkt       timestamp                null,
    registrerttidspunkt    timestamp                null,
    ferdigbehandlettidspunkt timestamp              null,
    vedtaktidspunkt        timestamp                null,
    utbetalttidspunkt      timestamp                null,
    endrettidspunkt        timestamp                not null,
    søknadsformat          varchar                  not null,
    forventetoppstarttidspunkt timestamp            not null,
    teknisktidspunkt       timestamp                null,
    sakytelse              varchar                  not null,
    sakutland              boolean                  not null,
    behandlingtype         varchar                  not null,
    behandlingstatus       varchar                  not null,
    behandlingresultat     varchar                  not null,
    resultatbegrunnelse    varchar                  not null,
    behandlingmetode       varchar                  not null,
    opprettetav            varchar                  not null,
    saksbehandler          varchar                  not null,
    ansvarligbeslutter     varchar                  not null,
    ansvarligenhet         varchar                  not null,
    tilbakekrevingsbeløp   decimal                  null,
    funksjonellperiodefom  date                     null,
    funksjonellperiodetom  date                     null,
    avsender               varchar                  not null,
    versjon                varchar                  not null
);

create table statistikk_sak_vilkår
(
    id                     varchar                  primary key,
    sak_id                 varchar                  null,
    vilkårid               varchar                  null,
    beskrivelse            varchar                  null,
    resultat               varchar                  null
);
