package no.nav.tiltakspenger.vedtak.meldinger

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Hendelse
import no.nav.tiltakspenger.vedtak.Skjerming

class SkjermingMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val ident: String,
    private val skjerming: Skjerming
) : Hendelse(aktivitetslogg) {

    override fun ident() = ident

    fun skjerming() = skjerming
}
