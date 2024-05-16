package no.nav.tiltakspenger.saksbehandling.domene.barnetillegg

import no.nav.tiltakspenger.felles.Periode

data class Periodisering<T>(val periodeMedVerdi: Map<Periode, T>) {
    fun <U> map(function: (T) -> U): Periodisering<U> {
        return Periodisering(
            periodeMedVerdi = this.periodeMedVerdi.mapValues { function(it.value) },
        )
    }

    fun utvidMedBlank(periode: Periode): Periodisering<T> {
        return TODO()
    }
}
