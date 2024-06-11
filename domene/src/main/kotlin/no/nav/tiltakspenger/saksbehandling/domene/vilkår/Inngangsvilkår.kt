package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde

sealed class Inngangsvilkår {

    abstract val tittel: String
    abstract val flateTittel: String
    abstract val lovReference: List<Lovreferanse>

    fun kilde(): Kilde =
        when (this) {
            ALDER -> Kilde.PDL
            INSTITUSJONSOPPHOLD -> Kilde.SØKNAD
            INTROPROGRAMMET -> Kilde.SØKNAD
            KVP -> Kilde.SØKNAD
            TILTAKSDELTAGELSE -> Kilde.ARENA
            LIVSOPPHOLDSYTELSER -> Kilde.FLERE
        }

    object ALDER : Inngangsvilkår() {
        override val tittel: String = "ALDER"
        override val flateTittel: String = "Alder"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.ALDER)
    }

    object INTROPROGRAMMET : Inngangsvilkår() {
        override val tittel: String = "INTROPROGRAMMET"
        override val flateTittel: String = "Introduksjonsprogrammet"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.INTROPROGRAMMET)
    }

    object KVP : Inngangsvilkår() {
        override val tittel: String = "KVP"
        override val flateTittel: String = "Kvalifiseringsprogrammet(KVP)"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.KVP)
    }

    object INSTITUSJONSOPPHOLD : Inngangsvilkår() {
        override val tittel: String = "INSTITUSJONSOPPHOLD"
        override val flateTittel: String = "Institusjonsopphold"
        override val lovReference: List<Lovreferanse> = listOf(Lovreferanse.INSTITUSJONSOPPHOLD)
    }

    object TILTAKSDELTAGELSE : Inngangsvilkår() {
        override val tittel: String = "TILTAKSDELTAGELSE"
        override val flateTittel: String = "Tiltaksdeltagelse"
        override val lovReference: List<Lovreferanse> =
            listOf(Lovreferanse.TILTAKSDELTAGELSE)
    }

    object LIVSOPPHOLDSYTELSER : Inngangsvilkår() {
        override val tittel: String = "TODO"
        override val flateTittel: String = "Todo"
        override val lovReference: List<Lovreferanse> =
            listOf(Lovreferanse.LIVSOPPHOLDSYTELSER)
    }
}
