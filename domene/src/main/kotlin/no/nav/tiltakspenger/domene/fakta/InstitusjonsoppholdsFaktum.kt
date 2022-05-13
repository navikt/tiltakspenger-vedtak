package no.nav.tiltakspenger.domene.fakta

import no.nav.tiltakspenger.domene.Faktum
import no.nav.tiltakspenger.domene.FaktumKilde
import no.nav.tiltakspenger.domene.Periode

class InstitusjonsoppholdsFaktum(
    val opphold: Boolean,
    kilde: FaktumKilde,
    val oppholdsperiode: Periode,
    val friKostOgLosji: Boolean
) : Faktum {
    override val kilde: FaktumKilde
        get() = TODO("Not yet implemented")

}
