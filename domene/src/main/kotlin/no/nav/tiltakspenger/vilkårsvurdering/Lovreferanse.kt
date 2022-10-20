package no.nav.tiltakspenger.vilkårsvurdering

enum class Lovreferanse(val lovverk: String, val paragraf: String, val ledd: String?, val beskrivelse: String) {
    AAP("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    DAGPENGER("Tiltakspengeforskriften", "§7", "1", "Forholdet til andre ytelser"),
    INTROPROGRAMMET("Tiltakspengeforskriften", "§7", "3", "Forholdet til andre ytelser"),
    KVP("Tiltakspengeforskriften", "§7", "3", "Forholdet til andre ytelser"),
    KOMMUNALE_YTELSER("Tiltakspengeforskriften", "§7", null, "Forholdet til andre ytelser"),
    STATLIGE_YTELSER("Tiltakspengeforskriften", "§7", null, "Forholdet til andre ytelser"),
}
