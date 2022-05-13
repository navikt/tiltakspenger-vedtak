package no.nav.tiltakspenger.domene.fakta

import no.nav.tiltakspenger.domene.Faktum
import no.nav.tiltakspenger.domene.FaktumKilde
import java.time.LocalDate

data class FødselsdatoFaktum(
    val fødselsdato: LocalDate,
    override val kilde: FaktumKilde
) : Faktum {
    fun erOver18() = true
    fun erUnder16() = false
}
