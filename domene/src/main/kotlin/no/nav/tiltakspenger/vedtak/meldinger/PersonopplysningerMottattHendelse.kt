package no.nav.tiltakspenger.vedtak.meldinger

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.ISøkerHendelse
import no.nav.tiltakspenger.vedtak.InnsendingHendelse
import no.nav.tiltakspenger.vedtak.Personopplysninger
import java.time.LocalDateTime

class PersonopplysningerMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val journalpostId: String,
    private val ident: String,
    private val personopplysninger: List<Personopplysninger>,
    private val tidsstempelPersonopplysningerInnhentet: LocalDateTime,
) : InnsendingHendelse(aktivitetslogg), ISøkerHendelse {

    override fun journalpostId() = journalpostId
    override fun ident() = ident

    fun personopplysninger() = personopplysninger

    fun tidsstempelPersonopplysningerInnhentet() = tidsstempelPersonopplysningerInnhentet
}
