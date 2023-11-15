package no.nav.tiltakspenger.vedtak.innsending.meldinger

import no.nav.tiltakspenger.vedtak.innsending.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.innsending.InnsendingHendelse
import no.nav.tiltakspenger.vedtak.innsending.YtelseSak
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
