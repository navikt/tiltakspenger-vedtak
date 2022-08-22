package no.nav.tiltakspenger.vedtak.meldinger

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Hendelse
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet

class ArenaTiltakMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val ident: String,
    private val tiltaksaktivitet: List<Tiltaksaktivitet>,
) : Hendelse(aktivitetslogg) {

    override fun ident() = ident

    fun tiltaksaktivitet() = tiltaksaktivitet
}