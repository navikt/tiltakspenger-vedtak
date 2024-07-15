package no.nav.tiltakspenger.vedtak.clients.person

import no.nav.tiltakspenger.libs.person.AdressebeskyttelseGradering.FORTROLIG
import no.nav.tiltakspenger.libs.person.AdressebeskyttelseGradering.STRENGT_FORTROLIG
import no.nav.tiltakspenger.libs.person.AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND
import no.nav.tiltakspenger.libs.person.BarnIFolkeregisteret
import no.nav.tiltakspenger.libs.person.BarnUtenFolkeregisteridentifikator
import no.nav.tiltakspenger.libs.person.Person
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerBarnMedIdent
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerBarnUtenIdent
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import java.time.LocalDate
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

private const val ALDER_BARNETILLEGG = 16L
private const val SIKKERHETSMARGIN_ÅR = 2L // søknaden sender med barn opp til 18 år. Vi lagrer det samme just in case
private fun BarnIFolkeregisteret.kanGiRettPåBarnetillegg() =
    fødselsdato.isAfter(LocalDate.now().minusYears(ALDER_BARNETILLEGG).minusYears(SIKKERHETSMARGIN_ÅR))

private fun BarnUtenFolkeregisteridentifikator.kanGiRettPåBarnetillegg() =
    fødselsdato?.isAfter(LocalDate.now().minusYears(ALDER_BARNETILLEGG).minusYears(SIKKERHETSMARGIN_ÅR)) ?: true
