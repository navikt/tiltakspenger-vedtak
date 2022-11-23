package no.nav.tiltakspenger.vedtak.meldinger

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingHendelse
import no.nav.tiltakspenger.vedtak.YtelseSak

class YtelserMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val journalpostId: String,
    private val ytelseSak: List<YtelseSak>,
) : InnsendingHendelse(aktivitetslogg) {

    override fun journalpostId() = journalpostId

    fun ytelseSak() = ytelseSak
}
