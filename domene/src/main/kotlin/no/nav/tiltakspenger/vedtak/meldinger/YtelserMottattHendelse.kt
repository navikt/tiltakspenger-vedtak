package no.nav.tiltakspenger.vedtak.meldinger

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Hendelse
import no.nav.tiltakspenger.vedtak.YtelseSak

class YtelserMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val ident: String,
    private val ytelseSak: List<YtelseSak>,
) : Hendelse(aktivitetslogg) {

    override fun ident() = ident

    fun ytelseSak() = ytelseSak
}