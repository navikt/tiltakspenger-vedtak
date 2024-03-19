package no.nav.tiltakspenger.innsending.domene.meldinger

import no.nav.tiltakspenger.innsending.domene.Aktivitetslogg
import no.nav.tiltakspenger.innsending.domene.InnsendingHendelse
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Tiltak
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
