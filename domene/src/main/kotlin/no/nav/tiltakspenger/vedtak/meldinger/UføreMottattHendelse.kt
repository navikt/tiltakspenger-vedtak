package no.nav.tiltakspenger.vedtak.meldinger

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingHendelse
import no.nav.tiltakspenger.vedtak.UføreVedtak
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
