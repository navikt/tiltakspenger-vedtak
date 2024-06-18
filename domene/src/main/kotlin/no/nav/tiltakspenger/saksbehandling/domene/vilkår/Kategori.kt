package no.nav.tiltakspenger.saksbehandling.domene.vilkår

enum class Kategori(val tittel: String, val vilkår: List<String>, val inngangsvilkår: Inngangsvilkår) {
    ALDER(
        "Alder",
        listOf(Inngangsvilkår.ALDER.tittel),
        Inngangsvilkår.ALDER,
    ),
    INTROKVP(
        "Introduksjonsprogrammet og Kvalifiseringsprogrammet",
        listOf(Inngangsvilkår.INTROPROGRAMMET.tittel, Inngangsvilkår.KVP.tittel),
        Inngangsvilkår.INTROPROGRAMMET,
    ),
    UTBETALINGER(
        "Utbetalinger",
        listOf(
            LivsoppholdDelVilkår.FORELDREPENGER.tittel,
            LivsoppholdDelVilkår.PLEIEPENGER_SYKT_BARN.tittel,
            LivsoppholdDelVilkår.PLEIEPENGER_NÆRSTÅENDE.tittel,
            LivsoppholdDelVilkår.ALDERSPENSJON.tittel,
            LivsoppholdDelVilkår.PENSJONSINNTEKT.tittel,
            LivsoppholdDelVilkår.ETTERLØNN.tittel,
            LivsoppholdDelVilkår.AAP.tittel,
            LivsoppholdDelVilkår.DAGPENGER.tittel,
            LivsoppholdDelVilkår.GJENLEVENDEPENSJON.tittel,
            LivsoppholdDelVilkår.FORELDREPENGER.tittel,
            LivsoppholdDelVilkår.JOBBSJANSEN.tittel,
            LivsoppholdDelVilkår.UFØRETRYGD.tittel,
            LivsoppholdDelVilkår.OMSORGSPENGER.tittel,
            LivsoppholdDelVilkår.OPPLÆRINGSPENGER.tittel,
            LivsoppholdDelVilkår.OVERGANGSSTØNAD.tittel,
            LivsoppholdDelVilkår.SYKEPENGER.tittel,
            LivsoppholdDelVilkår.SVANGERSKAPSPENGER.tittel,
            LivsoppholdDelVilkår.SUPPLERENDESTØNADALDER.tittel,
            LivsoppholdDelVilkår.SUPPLERENDESTØNADFLYKTNING.tittel,
        ),
        Inngangsvilkår.LIVSOPPHOLDSYTELSER,
    ),
    INSTITUSJONSOPPHOLD(
        "Institusjonsopphold",
        listOf(Inngangsvilkår.INSTITUSJONSOPPHOLD.tittel),
        Inngangsvilkår.INSTITUSJONSOPPHOLD,
    )
}
