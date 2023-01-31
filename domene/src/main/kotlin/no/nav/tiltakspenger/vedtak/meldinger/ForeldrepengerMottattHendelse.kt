package no.nav.tiltakspenger.vedtak.meldinger

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.ForeldrepengerVedtak
import no.nav.tiltakspenger.vedtak.InnsendingHendelse
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
