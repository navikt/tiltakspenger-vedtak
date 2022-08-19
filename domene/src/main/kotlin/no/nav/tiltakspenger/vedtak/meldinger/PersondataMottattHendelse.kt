package no.nav.tiltakspenger.vedtak.meldinger

import java.time.LocalDate
import java.time.LocalDateTime
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Hendelse
import no.nav.tiltakspenger.vedtak.Personinfo

class PersondataMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val ident: String,
    private val personinfo: Personinfo,
) : Hendelse(aktivitetslogg) {

    override fun ident() = ident

    fun personinfo() = personinfo
}