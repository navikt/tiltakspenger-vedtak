package no.nav.tiltakspenger.innsending.meldinger

import no.nav.tiltakspenger.innsending.Aktivitetslogg
import no.nav.tiltakspenger.innsending.SøkerHendelse

class IdentMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val ident: String,
) : SøkerHendelse(aktivitetslogg) {

    override fun ident() = ident
}
