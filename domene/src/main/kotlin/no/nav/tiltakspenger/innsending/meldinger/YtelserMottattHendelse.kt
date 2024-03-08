package no.nav.tiltakspenger.innsending.meldinger

import no.nav.tiltakspenger.innsending.Aktivitetslogg
import no.nav.tiltakspenger.innsending.InnsendingHendelse
import no.nav.tiltakspenger.innsending.YtelseSak
import java.time.LocalDateTime

class YtelserMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val journalpostId: String,
    private val ytelseSak: List<YtelseSak>,
    private val tidsstempelYtelserInnhentet: LocalDateTime,
) : InnsendingHendelse(aktivitetslogg) {

    override fun journalpostId() = journalpostId

    fun ytelseSak() = ytelseSak

    fun tidsstempelYtelserInnhentet() = tidsstempelYtelserInnhentet
}
