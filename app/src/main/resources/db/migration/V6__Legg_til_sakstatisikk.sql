create table statistikk_sak
(
    id                     serial                   PRIMARY KEY, -- må være stigende
    sak_id                 VARCHAR                  NOT NULL,
    saksnummer             VARCHAR                  NOT NULL,
    behandlingId           VARCHAR                  NOT NULL,
    relatertBehandlingId   VARCHAR                  NULL, -- hvis revurdering
    ident                  VARCHAR                  NOT NULL, -- fnr hva med kode 6?
    mottattTidspunkt       TIMESTAMP                NULL, -- tidspunkt for mottak av søknad
    registrertTidspunkt    TIMESTAMP                NULL, -- tidspunkt for registrering av sak
    ferdigBehandletTidspunkt TIMESTAMP              NULL, -- tidspunkt for ferdigbehandling av sak
    vedtakTidspunkt        TIMESTAMP                NOT NULL, -- tidspunkt for vedtak   (hva er forskjellen på ferdigBehandletTidspunkt og vedtakTidspunkt?)
    utbetaltTidspunkt      TIMESTAMP                NOT NULL, -- tidspunkt for første utbetaling
    endretTidspunkt        TIMESTAMP                NOT NULL, -- tidspunkt for siste endring av behandlingen
    søknadsformat          VARCHAR                  NOT NULL, -- papir, digital
    forventetOppstartTidspunkt TIMESTAMP            NOT NULL, -- forventet oppstart av tiltak
    tekniskTidspunkt       TIMESTAMP                NOT NULL, -- tidspunkt for denne raden
    sakYtelse              VARCHAR                  NOT NULL, -- hva slags ytelse (TILTAKSPENGER)
    sakUtland              BOOLEAN                  NOT NULL, -- om saken gjelder utland (står som varchar uppercase i dokumentasjonen?)
    behandlingType         VARCHAR                  NOT NULL, -- SØKNAD, REVURDERING, GJENOPPTAK, NY_RETTIGHET, KLAGE, ANKE
    behandlingStatus       VARCHAR                  NOT NULL, -- UNDER_BEHANDLING, FERDIG_BEHANDLET, VEDTAK_FATTET, UTBETALT, AVSLUTTET (dette er bare forslag)
    behandlingResultat     VARCHAR                  NOT NULL, -- INNVILGET, AVSLÅTT, AVVIST, HENLAGT, DELVIS_INNVILGET
    resultatBegrunnelse    VARCHAR                  NOT NULL, -- begrunnelse for resultat
    behandlingMetode       VARCHAR                  NOT NULL, -- manuell, automatisk
    -- disse skal bli satt til -5 hvis kode 6
    opprettetAv            VARCHAR                  NOT NULL, -- hvem som opprettet saken
    saksbehandler          VARCHAR                  NOT NULL, -- hvem som har behandlet saken
    ansvarligBeslutter     VARCHAR                  NOT NULL, -- hvem som har besluttet saken
    ansvarligEnhet         VARCHAR                  NOT NULL, -- hvilken enhet som har ansvar for saken

    tilbakekrevingsbeløp    DECIMAL                 NOT NULL, -- beløp som skal tilbakekreves
    funksjonellPeriodeFom  DATE                     NOT NULL, -- funksjonell periode for tilbakekreving
    funksjonellPeriodeTom  DATE                     NOT NULL, -- funksjonell periode for tilbakekreving
    avsender               VARCHAR                  NOT NULL, -- tpts
    versjon                VARCHAR                  NOT NULL  -- commit hash
);

create table statistikk_sak_vilkår
(
    id                     VARCHAR                  PRIMARY KEY, -- må være stigende
    sak_id                 VARCHAR                  NOT NULL, -- fremmednøkkel til statistikk_sak
    vilkårId               VARCHAR                  NOT NULL, -- hva slags vilkår
    beskrivelse            VARCHAR                  NOT NULL, -- beskrivelse av vilkår
    resultat               VARCHAR                  NOT NULL  -- GODKJENT, AVSLÅTT
);
