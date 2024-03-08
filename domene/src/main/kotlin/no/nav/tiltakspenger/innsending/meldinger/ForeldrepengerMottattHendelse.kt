package no.nav.tiltakspenger.innsending.meldinger

import no.nav.tiltakspenger.innsending.Aktivitetslogg
import no.nav.tiltakspenger.innsending.ForeldrepengerVedtak
import no.nav.tiltakspenger.innsending.InnsendingHendelse
import java.time.LocalDateTime

class ForeldrepengerMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val journalpostId: String,
    private val ident: String,
    private val foreldrepengerVedtakListe: List<ForeldrepengerVedtak>,
    private val tidsstempelForeldrepengerVedtakInnhentet: LocalDateTime,
) : InnsendingHendelse(aktivitetslogg) {

    override fun journalpostId() = journalpostId
    fun ident() = ident

    fun foreldrepengerVedtakListe() = foreldrepengerVedtakListe

    fun tidsstempelForeldrepengerVedtakInnhentet() = tidsstempelForeldrepengerVedtakInnhentet
}
