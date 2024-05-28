package no.nav.tiltakspenger.saksbehandling.domene.vilkår

enum class Kategori(val tittel: String, val vilkår: List<Vilkår>) {
    ALDER("Alder", listOf(Vilkår.ALDER)),
    INTROKVP(
        "Introduksjonsprogrammet og Kvalifiseringsprogrammet",
        listOf(Vilkår.INTROPROGRAMMET, Vilkår.KVP),
    ),
    UTBETALINGER(
        "Utbetalinger",
        listOf(
            Vilkår.FORELDREPENGER,
            Vilkår.PLEIEPENGER_SYKT_BARN,
            Vilkår.PLEIEPENGER_NÆRSTÅENDE,
            Vilkår.ALDERSPENSJON,
            Vilkår.PENSJONSINNTEKT,
            Vilkår.ETTERLØNN,
            Vilkår.AAP,
            Vilkår.DAGPENGER,
            Vilkår.GJENLEVENDEPENSJON,
            Vilkår.FORELDREPENGER,
            Vilkår.JOBBSJANSEN,
            Vilkår.UFØRETRYGD,
            Vilkår.OMSORGSPENGER,
            Vilkår.OPPLÆRINGSPENGER,
            Vilkår.OVERGANGSSTØNAD,
            Vilkår.SYKEPENGER,
            Vilkår.SVANGERSKAPSPENGER,
            Vilkår.SUPPLERENDESTØNADFLYKTNING,
        ),
    ),
    INSTITUSJONSOPPHOLD("Institusjonsopphold", listOf(Vilkår.INSTITUSJONSOPPHOLD)),
}
