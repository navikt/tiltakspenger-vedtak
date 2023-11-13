package no.nav.tiltakspenger.vedtak.meldinger

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingHendelse
import no.nav.tiltakspenger.vedtak.Tiltak
import java.time.LocalDateTime

class TiltakMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val journalpostId: String,
    private val tiltaks: List<Tiltak>,
    private val tidsstempelTiltakInnhentet: LocalDateTime,
) : InnsendingHendelse(aktivitetslogg) {

    override fun journalpostId() = journalpostId

    fun tiltaksaktivitet() = tiltaks

    fun tidsstempelTiltakInnhentet() = tidsstempelTiltakInnhentet
}
