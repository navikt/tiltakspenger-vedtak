package no.nav.tiltakspenger.innsending.domene.meldinger

import no.nav.tiltakspenger.innsending.domene.Aktivitetslogg
import no.nav.tiltakspenger.innsending.domene.InnsendingHendelse
import no.nav.tiltakspenger.innsending.domene.UføreVedtak
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
