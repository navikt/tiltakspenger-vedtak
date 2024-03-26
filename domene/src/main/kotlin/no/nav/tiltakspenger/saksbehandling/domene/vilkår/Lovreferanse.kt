package no.nav.tiltakspenger.saksbehandling.domene.vilkår

// i stor grad hentet fra https://lovdata.no/pro/#document/SF/forskrift/2013-11-04-1286
enum class LovreferanseTilVilkår(val lovverk: String, val paragraf: String, val ledd: String, val beskrivelse: String) {
    ALDER("Tiltakspengeforskriften", "§3", "1", "Tiltakspenger og barnetillegg"),
    TILTAKSPENGER("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    AAP("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    DAGPENGER("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    SYKEPENGER("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    UFØRETRYGD("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    OVERGANGSSTØNAD("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    PLEIEPENGER_NÆRSTÅENDE("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    PLEIEPENGER_SYKT_BARN("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    FORELDREPENGER("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    SVANGERSKAPSPENGER("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    GJENLEVENDEPENSJON("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    SUPPLERENDESTØNAD_FLYKTNING("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    SUPPLERENDESTØNAD_ALDER("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    ALDERSPENSJON("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    OPPLÆRINGSPENGER("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    OMSORGSPENGER("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    JOBBSJANSEN("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    INTROPROGRAMMET("Tiltakspengeforskriften", "§7", "3", "Forholdet til andre ytelser"),
    KVP("Tiltakspengeforskriften", "§7", "3", "Forholdet til andre ytelser"),
    INSTITUSJONSOPPHOLD("Tiltakspengeforskriften", "§9", "1", "Opphold i institusjon, fengsel mv."),
    PENSJONSINNTEKT("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    LØNNSINNTEKT("Rundskriv om tiltakspenger", "§8", "1", "Forholdet til lønn"),
    ETTERLØNNARBEIDSMARKEDSLOV("Arbeidsmarkedsloven", "§13", "1", "Ytelser til gjennomføring av arbeidsmarkedstiltak mv."),
    ETTERLØNNRUNDSKRIV("Rundskriv om tiltakspenger", "§8", "1", "Forholdet til lønn"),
}

enum class LovreferanseTilKatogeri(val lovverk: String, val paragraf: String, val beskrivelse: String) {
    FORSKRIFT_P7("Tiltakspengeforskriften", "§7", "Forholdet til andre ytelser"),
    FORSKRIFT_P3("Tiltakspengeforskriften", "§3", "Tiltakspenger og barnetillegg"),
    FORSKRIFT_P9("Tiltakspengeforskriften", "§9", "Opphold i institusjon, fengsel mv."),
    RUNDSKRIV_P8("Rundskriv om tiltakspenger", "§8", "Forholdet til lønn"),
    ARBEIDSMARKEDSLOVEN_P13("Arbeidsmarkedsloven", "§13", "Ytelser til gjennomføring av arbeidsmarkedstiltak mv."),
}
