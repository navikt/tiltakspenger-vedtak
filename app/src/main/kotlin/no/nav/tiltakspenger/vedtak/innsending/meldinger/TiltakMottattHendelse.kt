package no.nav.tiltakspenger.vedtak.innsending.meldinger

import no.nav.tiltakspenger.domene.behandling.Tiltak
import no.nav.tiltakspenger.vedtak.innsending.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.innsending.InnsendingHendelse
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
