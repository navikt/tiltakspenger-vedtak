package no.nav.tiltakspenger.vedtak.innsending.meldinger

import no.nav.tiltakspenger.vedtak.innsending.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.innsending.SøkerHendelse

class IdentMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val ident: String,
) : SøkerHendelse(aktivitetslogg) {

    override fun ident() = ident
}
