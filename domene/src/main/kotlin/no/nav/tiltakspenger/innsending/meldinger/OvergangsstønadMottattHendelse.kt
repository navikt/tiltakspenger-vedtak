package no.nav.tiltakspenger.innsending.meldinger

import no.nav.tiltakspenger.innsending.Aktivitetslogg
import no.nav.tiltakspenger.innsending.InnsendingHendelse
import no.nav.tiltakspenger.innsending.OvergangsstønadVedtak
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
