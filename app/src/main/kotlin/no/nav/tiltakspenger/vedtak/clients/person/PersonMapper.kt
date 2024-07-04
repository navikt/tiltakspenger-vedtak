package no.nav.tiltakspenger.vedtak.clients.person

import no.nav.tiltakspenger.libs.person.AdressebeskyttelseGradering.FORTROLIG
import no.nav.tiltakspenger.libs.person.AdressebeskyttelseGradering.STRENGT_FORTROLIG
import no.nav.tiltakspenger.libs.person.AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND
import no.nav.tiltakspenger.libs.person.Person
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerBarnMedIdent
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerBarnUtenIdent
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.vedtak.routes.rivers.kanGiRettPåBarnetillegg
import java.time.LocalDateTime

/**
 * Har ansvar for å mappe fra tiltakspenger-libs sitt Person-objekt til en liste av Personopplysninger-objekter.
 * Kopiert fra no.nav.tiltakspenger.vedtak.routes.rivers.PersonopplysningerRoutes.kt
 */
internal fun mapPersonopplysninger(
    dto: Person,
    innhentet: LocalDateTime,
    ident: String,
): List<Personopplysninger> {
    return dto.barn.filter { it.kanGiRettPåBarnetillegg() }.map {
        PersonopplysningerBarnMedIdent(
            ident = it.ident,
            fødselsdato = it.fødselsdato,
            fornavn = it.fornavn,
            mellomnavn = it.mellomnavn,
            etternavn = it.etternavn,
            fortrolig = it.adressebeskyttelseGradering == FORTROLIG,
            strengtFortrolig = it.adressebeskyttelseGradering == STRENGT_FORTROLIG,
            strengtFortroligUtland = dto.adressebeskyttelseGradering == STRENGT_FORTROLIG_UTLAND,
            skjermet = null,
            oppholdsland = null, // TODO: fix!
            tidsstempelHosOss = innhentet,
        )
    } + dto.barnUtenFolkeregisteridentifikator.filter { it.kanGiRettPåBarnetillegg() }.map { barn ->
        PersonopplysningerBarnUtenIdent(
            fødselsdato = barn.fødselsdato,
            fornavn = barn.fornavn,
            mellomnavn = barn.mellomnavn,
            etternavn = barn.etternavn,
            tidsstempelHosOss = innhentet,
        )
    } + PersonopplysningerSøker(
        ident = ident,
        fødselsdato = dto.fødselsdato,
        fornavn = dto.fornavn,
        mellomnavn = dto.mellomnavn,
        etternavn = dto.etternavn,
        fortrolig = dto.adressebeskyttelseGradering == FORTROLIG,
        strengtFortrolig = dto.adressebeskyttelseGradering == STRENGT_FORTROLIG,
        strengtFortroligUtland = dto.adressebeskyttelseGradering == STRENGT_FORTROLIG_UTLAND,
        skjermet = null,
        kommune = dto.gtKommune,
        bydel = dto.gtBydel,
        tidsstempelHosOss = innhentet,
    )
}
