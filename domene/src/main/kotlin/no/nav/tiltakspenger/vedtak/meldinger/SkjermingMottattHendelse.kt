package no.nav.tiltakspenger.vedtak.meldinger

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.ISøkerHendelse
import no.nav.tiltakspenger.vedtak.InnsendingHendelse
import no.nav.tiltakspenger.vedtak.Skjerming
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
