package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde

sealed class Vilkår {

    abstract val tittel: String
    abstract val flateTittel: String
    abstract val lovReference: List<Lovreferanse>

    fun kilde(): Kilde =
        when (this) {
            AAP -> Kilde.ARENA
            ALDER -> Kilde.PDL
            ALDERSPENSJON -> Kilde.SØKNAD
            DAGPENGER -> Kilde.ARENA
            FORELDREPENGER -> Kilde.FPSAK
            GJENLEVENDEPENSJON -> Kilde.SØKNAD
            INSTITUSJONSOPPHOLD -> Kilde.SØKNAD
            INTROPROGRAMMET -> Kilde.SØKNAD
            JOBBSJANSEN -> Kilde.SØKNAD
            KVP -> Kilde.SØKNAD
            LØNNSINNTEKT -> Kilde.SØKNAD
            OMSORGSPENGER -> Kilde.K9SAK
            OPPLÆRINGSPENGER -> Kilde.K9SAK
            OVERGANGSSTØNAD -> Kilde.EF
            PENSJONSINNTEKT -> Kilde.SØKNAD
            PLEIEPENGER_NÆRSTÅENDE -> Kilde.K9SAK
            PLEIEPENGER_SYKT_BARN -> Kilde.K9SAK
            SUPPLERENDESTØNADALDER -> Kilde.SØKNAD
            SUPPLERENDESTØNADFLYKTNING -> Kilde.SØKNAD
            SVANGERSKAPSPENGER -> Kilde.FPSAK
            SYKEPENGER -> Kilde.SØKNAD
            TILTAKSPENGER -> Kilde.ARENA
            UFØRETRYGD -> Kilde.PESYS
            ETTERLØNN -> Kilde.SØKNAD
        }

    object ALDER : Vilkår() {
        override val tittel: String = "ALDER"
        override val flateTittel: String = "Alder"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.ALDER)
    }

    object TILTAKSPENGER : Vilkår() {
        override val tittel: String = "TILTAKSPENGER"
        override val flateTittel: String = "Tiltakspenger"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.TILTAKSPENGER)
    }

    object JOBBSJANSEN : Vilkår() {
        override val tittel: String = "JOBBSJANSEN"
        override val flateTittel: String = "Jobbsjansen"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.JOBBSJANSEN)
    }

    object AAP : Vilkår() {
        override val tittel: String = "AAP"
        override val flateTittel: String = "AAP"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.AAP)
    }

    object DAGPENGER : Vilkår() {
        override val tittel: String = "DAGPENGER"
        override val flateTittel: String = "Dagpenger"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.DAGPENGER)
    }

    object SYKEPENGER : Vilkår() {
        override val tittel: String = "SYKEPENGER"
        override val flateTittel: String = "Sykepenger"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.SYKEPENGER)
    }

    object UFØRETRYGD : Vilkår() {
        override val tittel: String = "UFØRETRYGD"
        override val flateTittel: String = "Uføretrygd"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.UFØRETRYGD)
    }

    object OVERGANGSSTØNAD : Vilkår() {
        override val tittel: String = "OVERGANGSSTØNAD"
        override val flateTittel: String = "Overgangsstønad"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.OVERGANGSSTØNAD)
    }

    object PLEIEPENGER_NÆRSTÅENDE : Vilkår() {
        override val tittel: String = "PLEIEPENGER_NÆRSTÅENDE"
        override val flateTittel: String = "Pleiepenger nærstående"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.PLEIEPENGER_NÆRSTÅENDE)
    }

    object PLEIEPENGER_SYKT_BARN : Vilkår() {
        override val tittel: String = "PLEIEPENGER_SYKT_BARN"
        override val flateTittel: String = "Pleiepenger sykt barn"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.PLEIEPENGER_SYKT_BARN)
    }

    object FORELDREPENGER : Vilkår() {
        override val tittel: String = "FORELDREPENGER"
        override val flateTittel: String = "Foreldrepenger"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.FORELDREPENGER)
    }

    object SVANGERSKAPSPENGER : Vilkår() {
        override val tittel: String = "SVANGERSKAPSPENGER"
        override val flateTittel: String = "Svangerskapspenger"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.SVANGERSKAPSPENGER)
    }

    object GJENLEVENDEPENSJON : Vilkår() {
        override val tittel: String = "GJENLEVENDEPENSJON"
        override val flateTittel: String = "Gjenlevendepensjon"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.GJENLEVENDEPENSJON)
    }

    object SUPPLERENDESTØNADFLYKTNING : Vilkår() {
        override val tittel: String = "SUPPLERENDESTØNADFLYKTNING"
        override val flateTittel: String = "Supplerende stønad flyktning"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.SUPPLERENDESTØNAD_FLYKTNING)
    }

    object SUPPLERENDESTØNADALDER : Vilkår() {
        override val tittel: String = "SUPPLERENDESTØNADALDER"
        override val flateTittel: String = "Supplerende stønad alder"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.SUPPLERENDESTØNAD_ALDER)
    }

    object ALDERSPENSJON : Vilkår() {
        override val tittel: String = "ALDERSPENSJON"
        override val flateTittel: String = "Alderpensjon"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.ALDERSPENSJON)
    }

    object OPPLÆRINGSPENGER : Vilkår() {
        override val tittel: String = "OPPLÆRINGSPENGER"
        override val flateTittel: String = "Opplæringspenger"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.OPPLÆRINGSPENGER)
    }

    object OMSORGSPENGER : Vilkår() {
        override val tittel: String = "OMSORGSPENGER"
        override val flateTittel: String = "Omsorgspenger"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.OMSORGSPENGER)
    }

    object INTROPROGRAMMET : Vilkår() {
        override val tittel: String = "INTROPROGRAMMET"
        override val flateTittel: String = "Introduksjonsprogrammet"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.INTROPROGRAMMET)
    }

    object KVP : Vilkår() {
        override val tittel: String = "KVP"
        override val flateTittel: String = "Kvalifiseringsprogrammet(KVP)"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.KVP)
    }

    object INSTITUSJONSOPPHOLD : Vilkår() {
        override val tittel: String = "INSTITUSJONSOPPHOLD"
        override val flateTittel: String = "Institusjonsopphold"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.INSTITUSJONSOPPHOLD)
    }

    object PENSJONSINNTEKT : Vilkår() {
        override val tittel: String = "PENSJONSINNTEKT"
        override val flateTittel: String = "Pensjonsinntekt"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.PENSJONSINNTEKT)
    }

    object LØNNSINNTEKT : Vilkår() {
        override val tittel: String = "LØNNSINNTEKT"
        override val flateTittel: String = "Lønnsinntekt"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.LØNNSINNTEKT)
    }

    object ETTERLØNN : Vilkår() {
        override val tittel: String = "ETTERLØNN"
        override val flateTittel: String = "Etterlønn"
        override val lovReference: List<Lovreferanse> =
            listOf(Lovreferanse.ETTERLØNNARBEIDSMARKEDSLOV, Lovreferanse.ETTERLØNNRUNDSKRIV)
    }
}
