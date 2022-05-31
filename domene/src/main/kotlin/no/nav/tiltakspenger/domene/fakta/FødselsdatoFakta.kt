package no.nav.tiltakspenger.domene.fakta

import java.time.LocalDate

data class FødselsdatoFakta(
    val system: FødselsDatoSystem? = null
) : Fakta<FødselsDatoSystem> {
    constructor(fødselsdato: LocalDate, kilde: FaktumKilde): this(
        system = FødselsDatoSystem(fødselsdato)
    )

    override fun leggTil(faktum: FødselsDatoSystem): Fakta<FødselsDatoSystem> {
        return this.copy(system = faktum)
    }
}

data class FødselsDatoSystem(
    val fødselsdato: LocalDate,
    override val kilde: FaktumKilde = FaktumKilde.SYSTEM,
) : Faktum
