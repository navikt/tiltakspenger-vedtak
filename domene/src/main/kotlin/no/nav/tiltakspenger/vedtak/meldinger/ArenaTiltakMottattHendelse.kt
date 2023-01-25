package no.nav.tiltakspenger.vedtak.meldinger

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingHendelse
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import java.time.LocalDateTime

class ArenaTiltakMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val journalpostId: String,
    private val tiltaksaktivitet: List<Tiltaksaktivitet>,
    private val tidsstempelTiltakInnhentet: LocalDateTime,
) : InnsendingHendelse(aktivitetslogg) {

    override fun journalpostId() = journalpostId

    fun tiltaksaktivitet() = tiltaksaktivitet

    fun tidsstempelTiltakInnhentet() = tidsstempelTiltakInnhentet
}
