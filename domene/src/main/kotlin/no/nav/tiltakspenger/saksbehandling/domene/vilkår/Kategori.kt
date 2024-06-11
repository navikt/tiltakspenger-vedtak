package no.nav.tiltakspenger.saksbehandling.domene.vilkår

enum class Kategori(val tittel: String, val vilkår: Inngangsvilkår) {
    ALDER("Alder", Inngangsvilkår.ALDER),
    INTROKVP(
        "Introduksjonsprogrammet og Kvalifiseringsprogrammet",
        Inngangsvilkår.INTROPROGRAMMET, // TODO En eller to?
    ),
    UTBETALINGER(
        "Utbetalinger",
        Inngangsvilkår.LIVSOPPHOLDSYTELSER,
    ),
    INSTITUSJONSOPPHOLD("Institusjonsopphold", Inngangsvilkår.INSTITUSJONSOPPHOLD),
}
