package no.nav.tiltakspenger.vedtak.meldinger

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Hendelse
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import java.time.LocalDateTime

class ArenaTiltakMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val ident: String,
    private val tiltaksaktivitet: List<Tiltaksaktivitet>,
    private val innhentet: LocalDateTime,
) : Hendelse(aktivitetslogg) {

    override fun ident() = ident

    fun tiltaksaktivitet() = tiltaksaktivitet
}