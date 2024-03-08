package no.nav.tiltakspenger.innsending.meldinger

import no.nav.tiltakspenger.innsending.Aktivitetslogg
import no.nav.tiltakspenger.innsending.ISøkerHendelse
import no.nav.tiltakspenger.innsending.InnsendingHendelse
import no.nav.tiltakspenger.saksbehandling.personopplysninger.Personopplysninger
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
