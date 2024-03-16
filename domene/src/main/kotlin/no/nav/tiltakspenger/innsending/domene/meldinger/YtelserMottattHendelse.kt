package no.nav.tiltakspenger.innsending.domene.meldinger

import no.nav.tiltakspenger.innsending.domene.Aktivitetslogg
import no.nav.tiltakspenger.innsending.domene.InnsendingHendelse
import no.nav.tiltakspenger.innsending.domene.YtelseSak
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
