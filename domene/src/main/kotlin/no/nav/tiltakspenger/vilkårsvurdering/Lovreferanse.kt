package no.nav.tiltakspenger.vilkårsvurdering

// i stor grad hentet fra https://lovdata.no/pro/#document/SF/forskrift/2013-11-04-1286
enum class Lovreferanse(val lovverk: String, val paragraf: String, val ledd: String, val beskrivelse: String) {
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
    INTROPROGRAMMET("Tiltakspengeforskriften", "§7", "3", "Forholdet til andre ytelser"),
    KVP("Tiltakspengeforskriften", "§7", "3", "Forholdet til andre ytelser"),
    KOMMUNALE_YTELSER("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    STATLIGE_YTELSER("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    INSTITUSJONSOPPHOLD("Tiltakspengeforskriften", "§9", "1", "Institusjonsopphold med kost og losji"),
    PENSJONSINNTEKT("Tiltakspengeforskriften", "§7", "1", "Institusjonsopphold med kost og losji"),
    LØNNSINNTEKT("Tiltakspengeforskriften", "§7", "1", "Institusjonsopphold med kost og losji"),
}
