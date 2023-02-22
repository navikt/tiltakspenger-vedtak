package no.nav.tiltakspenger.vedtak.meldinger

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingHendelse
import no.nav.tiltakspenger.vedtak.OvergangsstønadVedtak
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
