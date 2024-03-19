package no.nav.tiltakspenger.innsending.domene.meldinger

import no.nav.tiltakspenger.innsending.domene.Aktivitetslogg
import no.nav.tiltakspenger.innsending.domene.ISøkerHendelse
import no.nav.tiltakspenger.innsending.domene.InnsendingHendelse
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger
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
