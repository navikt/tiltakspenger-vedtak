package no.nav.tiltakspenger.vedtak.meldinger

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Hendelse
import no.nav.tiltakspenger.vedtak.Personopplysninger

class PersonopplysningerMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val ident: String,
    private val personopplysninger: Personopplysninger,
) : Hendelse(aktivitetslogg) {

    override fun ident() = ident

    fun personopplysninger() = personopplysninger
}
