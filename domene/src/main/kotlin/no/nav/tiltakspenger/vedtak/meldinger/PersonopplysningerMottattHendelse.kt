package no.nav.tiltakspenger.vedtak.meldinger

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Hendelse
import no.nav.tiltakspenger.vedtak.Personopplysninger

class PersonopplysningerMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val journalpostId: String,
    private val personopplysninger: List<Personopplysninger>,
) : Hendelse(aktivitetslogg) {

    override fun journalpostId() = journalpostId

    fun personopplysninger() = personopplysninger

}
