package no.nav.tiltakspenger.domene.fakta

import java.time.LocalDate

data class FødselsdatoFaktum(
    override val kilde: FaktumKilde,
    val fødselsdato: LocalDate,
) : Faktum {
    fun erOver18() = true
    fun erUnder16() = false
}
