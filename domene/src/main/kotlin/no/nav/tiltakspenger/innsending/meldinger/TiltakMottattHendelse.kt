package no.nav.tiltakspenger.innsending.meldinger

import no.nav.tiltakspenger.innsending.Aktivitetslogg
import no.nav.tiltakspenger.innsending.InnsendingHendelse
import no.nav.tiltakspenger.saksbehandling.behandling.Tiltak
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
