package no.nav.tiltakspenger.vedtak.innsending.meldinger

import no.nav.tiltakspenger.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.vedtak.innsending.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.innsending.ISøkerHendelse
import no.nav.tiltakspenger.vedtak.innsending.InnsendingHendelse
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
