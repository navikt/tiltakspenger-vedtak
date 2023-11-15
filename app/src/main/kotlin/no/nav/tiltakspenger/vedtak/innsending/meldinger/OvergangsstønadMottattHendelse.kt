package no.nav.tiltakspenger.vedtak.innsending.meldinger

import no.nav.tiltakspenger.vedtak.innsending.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.innsending.InnsendingHendelse
import no.nav.tiltakspenger.vedtak.innsending.OvergangsstønadVedtak
import java.time.LocalDateTime

class OvergangsstønadMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val journalpostId: String,
    private val ident: String,
    private val overgangsstønadVedtakListe: List<OvergangsstønadVedtak>,
    private val innhentet: LocalDateTime,
) : InnsendingHendelse(aktivitetslogg) {
    override fun journalpostId() = journalpostId
    fun ident() = ident

    fun overgangsstønadVedtakListe() = overgangsstønadVedtakListe

    fun tidsstempelOvergangsstønadVedtakInnhentet() = innhentet
}
