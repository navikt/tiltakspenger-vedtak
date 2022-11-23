package no.nav.tiltakspenger.vedtak.meldinger

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingHendelse
import no.nav.tiltakspenger.vedtak.Skjerming

class SkjermingMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val journalpostId: String,
    private val skjerming: Skjerming
) : InnsendingHendelse(aktivitetslogg) {

    override fun journalpostId() = journalpostId

    fun skjerming() = skjerming
}
