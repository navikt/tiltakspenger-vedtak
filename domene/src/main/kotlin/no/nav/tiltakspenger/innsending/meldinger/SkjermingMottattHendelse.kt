package no.nav.tiltakspenger.innsending.meldinger

import no.nav.tiltakspenger.innsending.Aktivitetslogg
import no.nav.tiltakspenger.innsending.ISøkerHendelse
import no.nav.tiltakspenger.innsending.InnsendingHendelse
import no.nav.tiltakspenger.innsending.Skjerming
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
