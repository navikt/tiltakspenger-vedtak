package no.nav.tiltakspenger.vilkårsvurdering

sealed class Vilkår {

    abstract val tittel: String
    abstract val lovreferanse: Lovreferanse

    object TILTAKSPENGER : Vilkår() {
        override val tittel: String = "TILTAKSPENGER"
        override val lovreferanse: Lovreferanse = Lovreferanse.TILTAKSPENGER
    }

    object AAP : Vilkår() {
        override val tittel: String = "AAP"
        override val lovreferanse: Lovreferanse = Lovreferanse.AAP
    }

    object DAGPENGER : Vilkår() {
        override val tittel: String = "DAGPENGER"
        override val lovreferanse: Lovreferanse = Lovreferanse.DAGPENGER
    }

    object SYKEPENGER : Vilkår() {
        override val tittel: String = "SYKEPENGER"
        override val lovreferanse: Lovreferanse = Lovreferanse.SYKEPENGER
    }

    object UFØRETRYGD : Vilkår() {
        override val tittel: String = "UFØRETRYGD"
        override val lovreferanse: Lovreferanse = Lovreferanse.UFØRETRYGD
    }

    object OVERGANGSSTØNAD : Vilkår() {
        override val tittel: String = "OVERGANGSSTØNAD"
        override val lovreferanse: Lovreferanse = Lovreferanse.OVERGANGSSTØNAD
    }

    object PLEIEPENGER : Vilkår() {
        override val tittel: String = "PLEIEPENGER"
        override val lovreferanse: Lovreferanse = Lovreferanse.PLEIEPENGER
    }

    object FORELDREPENGER : Vilkår() {
        override val tittel: String = "FORELDREPENGER"
        override val lovreferanse: Lovreferanse = Lovreferanse.FORELDREPENGER
    }

    object SVANGERSKAPSPENGER : Vilkår() {
        override val tittel: String = "SVANGERSKAPSPENGER"
        override val lovreferanse: Lovreferanse = Lovreferanse.SVANGERSKAPSPENGER
    }

    object GJENLEVENDEPENSJON : Vilkår() {
        override val tittel: String = "GJENLEVENDEPENSJON"
        override val lovreferanse: Lovreferanse = Lovreferanse.GJENLEVENDEPENSJON
    }

    object SUPPLERENDESTØNAD : Vilkår() {
        override val tittel: String = "SUPPLERENDESTØNAD"
        override val lovreferanse: Lovreferanse = Lovreferanse.SUPPLERENDESTØNAD
    }

    object ALDERSPENSJON : Vilkår() {
        override val tittel: String = "ALDERSPENSJON"
        override val lovreferanse: Lovreferanse = Lovreferanse.ALDERSPENSJON
    }

    object OPPLÆRINGSPENGER : Vilkår() {
        override val tittel: String = "OPPLÆRINGSPENGER"
        override val lovreferanse: Lovreferanse = Lovreferanse.OPPLÆRINGSPENGER
    }

    object OMSORGSPENGER : Vilkår() {
        override val tittel: String = "OMSORGSPENGER"
        override val lovreferanse: Lovreferanse = Lovreferanse.OMSORGSPENGER
    }

    object INTROPROGRAMMET : Vilkår() {
        override val tittel: String = "INTROPROGRAMMET"
        override val lovreferanse: Lovreferanse = Lovreferanse.INTROPROGRAMMET
    }

    object KVP : Vilkår() {
        override val tittel: String = "KVP"
        override val lovreferanse: Lovreferanse = Lovreferanse.KVP
    }

    object KOMMUNALEYTELSER : Vilkår() {
        override val tittel: String = "KOMMUNALE YTELSER"
        override val lovreferanse: Lovreferanse = Lovreferanse.KOMMUNALE_YTELSER
    }

    object STATLIGEYTELSER : Vilkår() {
        override val tittel: String = "STATLIGE YTELSER"
        override val lovreferanse: Lovreferanse = Lovreferanse.STATLIGE_YTELSER
    }

    object INSTITUSJONSOPPHOLD : Vilkår() {
        override val tittel: String = "INSTITUSJONSOPPHOLD"
        override val lovreferanse: Lovreferanse = Lovreferanse.INSTITUSJONSOPPHOLD
    }

    object PENSJONSINNTEKT : Vilkår() {
        override val tittel: String = "PENSJONSINNTEKT"
        override val lovreferanse: Lovreferanse = Lovreferanse.PENSJONSINNTEKT
    }

    object LØNNSINNTEKT : Vilkår() {
        override val tittel: String = "LØNNSINNTEKT"
        override val lovreferanse: Lovreferanse = Lovreferanse.LØNNSINNTEKT
    }

}
