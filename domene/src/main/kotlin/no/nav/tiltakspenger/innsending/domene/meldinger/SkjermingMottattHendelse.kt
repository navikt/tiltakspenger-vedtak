package no.nav.tiltakspenger.innsending.domene.meldinger

import no.nav.tiltakspenger.innsending.domene.Aktivitetslogg
import no.nav.tiltakspenger.innsending.domene.ISøkerHendelse
import no.nav.tiltakspenger.innsending.domene.InnsendingHendelse
import no.nav.tiltakspenger.saksbehandling.domene.skjerming.Skjerming
import java.time.LocalDateTime

class SkjermingMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val journalpostId: String,
    private val ident: String,
    private val skjerming: Skjerming,
    private val tidsstempelSkjermingInnhentet: LocalDateTime,
) : InnsendingHendelse(aktivitetslogg), ISøkerHendelse {

    override fun journalpostId() = journalpostId
    override fun ident() = ident

    fun skjerming() = skjerming

    fun tidsstempelSkjermingInnhentet() = tidsstempelSkjermingInnhentet
}
