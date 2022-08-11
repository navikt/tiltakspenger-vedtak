package no.nav.tiltakspenger.vedtak.meldinger

import java.time.LocalDate
import java.time.LocalDateTime
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Hendelse
import no.nav.tiltakspenger.vedtak.Personinfo

class PersondataMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val ident: String,
    private val fødselsdato: LocalDate,
    private val fornavn: String,
    private val mellomnavn: String?,
    private val etternavn: String,
    private val fortrolig: Boolean,
    private val strengtFortrolig: Boolean,
    private val innhentet: LocalDateTime,
) : Hendelse(aktivitetslogg) {

    override fun ident() = ident

    fun personinfo(): Personinfo = Personinfo(
        ident = ident,
        fødselsdato = fødselsdato,
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn,
        fortrolig = fortrolig,
        strengtFortrolig = strengtFortrolig,
        innhentet = innhentet,
    )
}