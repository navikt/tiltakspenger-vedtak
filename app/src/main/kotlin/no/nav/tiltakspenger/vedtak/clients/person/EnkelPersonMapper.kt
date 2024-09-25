package no.nav.tiltakspenger.vedtak.clients.person

import arrow.core.getOrElse
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.personklient.pdl.dto.AdressebeskyttelseGradering
import no.nav.tiltakspenger.libs.personklient.pdl.dto.Navn
import no.nav.tiltakspenger.libs.personklient.pdl.dto.PdlPerson
import no.nav.tiltakspenger.libs.personklient.pdl.dto.avklarGradering
import no.nav.tiltakspenger.libs.personklient.pdl.dto.avklarNavn
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.EnkelPerson

internal data class PdlResponse(
    val hentPerson: PdlPerson,
)

fun String.toEnkelPerson(
    fnr: Fnr,
): EnkelPerson {
    val data: PdlResponse = objectMapper.readValue<PdlResponse>(this)
    val person: PdlPerson = data.hentPerson
    val navn: Navn = avklarNavn(person.navn).getOrElse { it.mapError() }
    val adressebeskyttelse: AdressebeskyttelseGradering =
        avklarGradering(person.adressebeskyttelse).getOrElse { it.mapError() }
    return EnkelPerson(
        fnr = fnr,
        fornavn = navn.fornavn,
        mellomnavn = navn.mellomnavn,
        etternavn = navn.etternavn,
        fortrolig = adressebeskyttelse.erFortrolig(),
        strengtFortrolig = adressebeskyttelse.erStrengtFortrolig(),
        strengtFortroligUtland = adressebeskyttelse.erStrengtFortroligUtland(),
        skjermet = null,
    )
}
