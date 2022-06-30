package no.nav.tiltakspenger.vedtak.meldinger

import java.time.LocalDate
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Hendelse
import no.nav.tiltakspenger.vedtak.Person

class PersondataMottattHendelse(
    aktivitetslogg: Aktivitetslogg,
    private val ident: String,
    private val fødselsdato: LocalDate,
    private val fornavn: String,
    private val mellomnavn: String?,
    private val etternavn: String,
    private val fortrolig: Boolean,
    private val strengtFortrolig: Boolean
) : Hendelse(aktivitetslogg) {

    override fun ident() = ident

    fun person(): Person = Person(
        ident = ident,
        fødselsdato = fødselsdato,
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn,
        fortrolig = fortrolig,
        strengtFortrolig = strengtFortrolig
    )
}