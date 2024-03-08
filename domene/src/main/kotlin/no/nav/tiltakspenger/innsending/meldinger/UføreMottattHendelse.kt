package no.nav.tiltakspenger.innsending.meldinger

import no.nav.tiltakspenger.innsending.Aktivitetslogg
import no.nav.tiltakspenger.innsending.InnsendingHendelse
import no.nav.tiltakspenger.innsending.UføreVedtak
import java.time.LocalDateTime

class UføreMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val journalpostId: String,
    private val ident: String,
    private val uføreVedtak: UføreVedtak,
    private val tidsstempelUføreVedtakInnhentet: LocalDateTime,
) : InnsendingHendelse(aktivitetslogg) {

    override fun journalpostId() = journalpostId
    fun ident() = ident

    fun uføreVedtak() = uføreVedtak

    fun tidsstempelUføreVedtakInnhentet() = tidsstempelUføreVedtakInnhentet
}
