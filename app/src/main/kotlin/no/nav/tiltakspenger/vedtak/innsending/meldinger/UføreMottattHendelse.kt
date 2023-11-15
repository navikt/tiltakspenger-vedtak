package no.nav.tiltakspenger.vedtak.innsending.meldinger

import no.nav.tiltakspenger.vedtak.innsending.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.innsending.InnsendingHendelse
import no.nav.tiltakspenger.vedtak.innsending.UføreVedtak
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
