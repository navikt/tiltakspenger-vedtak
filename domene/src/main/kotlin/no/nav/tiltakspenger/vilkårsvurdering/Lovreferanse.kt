package no.nav.tiltakspenger.vilkårsvurdering

// i stor grad hentet fra https://lovdata.no/pro/#document/SF/forskrift/2013-11-04-1286
enum class Lovreferanse(val lovverk: String, val paragraf: String, val ledd: String?, val beskrivelse: String) {
    AAP("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    DAGPENGER("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    SYKEPENGER("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    INTROPROGRAMMET("Tiltakspengeforskriften", "§7", "3", "Forholdet til andre ytelser"),
    KVP("Tiltakspengeforskriften", "§7", "3", "Forholdet til andre ytelser"),
    KOMMUNALE_YTELSER("Tiltakspengeforskriften", "§7", null, "Forholdet til andre ytelser"),
    STATLIGE_YTELSER("Tiltakspengeforskriften", "§7", null, "Forholdet til andre ytelser"),
}
