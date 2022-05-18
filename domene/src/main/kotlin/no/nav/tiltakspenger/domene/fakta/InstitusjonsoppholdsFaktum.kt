package no.nav.tiltakspenger.domene.fakta

import no.nav.tiltakspenger.domene.Periode

class InstitusjonsoppholdsFaktum(
    override val kilde: FaktumKilde,
    val opphold: Boolean,
    val oppholdsperiode: List<Periode>,
    val friKostOgLosji: Boolean
) : Faktum