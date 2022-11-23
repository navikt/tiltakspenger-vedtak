package no.nav.tiltakspenger.vedtak.meldinger

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingHendelse
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet

class ArenaTiltakMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val journalpostId: String,
    private val tiltaksaktivitet: List<Tiltaksaktivitet>?,
    private val feil: Feilmelding? = null,
) : InnsendingHendelse(aktivitetslogg) {

    override fun journalpostId() = journalpostId

    fun tiltaksaktivitet() = tiltaksaktivitet

    fun feilmelding() = feil

    enum class Feilmelding(val message: String) {
        PersonIkkeFunnet("Fant ikke person i PDL")
    }
}
