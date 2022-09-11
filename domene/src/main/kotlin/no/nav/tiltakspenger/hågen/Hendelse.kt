package no.nav.tiltakspenger.hågen

import no.nav.tiltakspenger.domene.Periode
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Hendelse

class KVPManuellVurderingHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val ident: String,
    private val manuellVurdering: Vurdering,
) : Hendelse(aktivitetslogg) {

    override fun ident() = ident

    fun manuellVurdering() = manuellVurdering
}

class ErOver18ÅrManuellVurderingHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val ident: String,
    private val manuellVurdering: Vurdering,
) : Hendelse(aktivitetslogg) {

    override fun ident() = ident

    fun manuellVurdering() = manuellVurdering
}

class NyPeriodeHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val ident: String,
    private val nyPeriode: Periode,
) : Hendelse(aktivitetslogg) {

    override fun ident() = ident

    fun nyPeriode() = nyPeriode
}
