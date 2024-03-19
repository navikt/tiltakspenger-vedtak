package no.nav.tiltakspenger.innsending.domene.meldinger

import no.nav.tiltakspenger.innsending.domene.Aktivitetslogg
import no.nav.tiltakspenger.innsending.domene.SøkerHendelse

class IdentMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val ident: String,
) : SøkerHendelse(aktivitetslogg) {

    override fun ident() = ident
}
