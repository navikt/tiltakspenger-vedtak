package no.nav.tiltakspenger.domene.fakta

import no.nav.tiltakspenger.domene.Periode

data class InstitusjonsoppholdsFakta (
    val bruker: InstitusjonsoppholdsFaktumBruker? = null,
    val system: InstitusjonsoppholdsFaktumSystem? = null,
    val saksbehandler: InstitusjonsoppholdsFaktumSaksbehandler? = null,
): Fakta<InstitusjonsoppholdsFaktum> {
    override fun leggTil(faktum: InstitusjonsoppholdsFaktum): Fakta<InstitusjonsoppholdsFaktum> {
        return when (faktum) {
            is InstitusjonsoppholdsFaktumBruker -> this.copy(bruker = faktum)
            is InstitusjonsoppholdsFaktumSystem -> this.copy(system = faktum)
            is InstitusjonsoppholdsFaktumSaksbehandler-> this.copy(saksbehandler = faktum)
            else -> throw IllegalArgumentException("Unexpected instance of InstitusjonsoppholdsFaktum")
        }
    }
}

interface InstitusjonsoppholdsFaktum : Faktum

class InstitusjonsoppholdsFaktumBruker(
    val opphold: Boolean,
    val oppholdsperiode: Periode,
    val friKostOgLosji: Boolean,
    override val kilde: FaktumKilde = FaktumKilde.BRUKER,
) : InstitusjonsoppholdsFaktum

class InstitusjonsoppholdsFaktumSystem(
    val opphold: Boolean,
    val oppholdsperiode: Periode,
    val friKostOgLosji: Boolean,
    override val kilde: FaktumKilde = FaktumKilde.SYSTEM,
) : InstitusjonsoppholdsFaktum

class InstitusjonsoppholdsFaktumSaksbehandler(
    val opphold: Boolean,
    val oppholdsperiode: Periode,
    val friKostOgLosji: Boolean,
    override val kilde: FaktumKilde = FaktumKilde.SAKSBEHANDLER,
) : InstitusjonsoppholdsFaktum
