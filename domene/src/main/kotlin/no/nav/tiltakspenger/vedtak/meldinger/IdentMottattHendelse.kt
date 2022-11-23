package no.nav.tiltakspenger.vedtak.meldinger

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.SøkerHendelse

class IdentMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val ident: String,
) : SøkerHendelse(aktivitetslogg) {

    override fun ident() = ident
}
