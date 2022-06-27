package no.nav.tiltakspenger.vedtak.meldinger

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Hendelse

class JoarkHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val ident: String,
) : Hendelse(aktivitetslogg) {

    override fun ident() = ident
}