package no.nav.tiltakspenger.vedtak.clients.person

import arrow.core.getOrElse
import arrow.core.raise.either
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.person.BarnIFolkeregisteret
import no.nav.tiltakspenger.libs.person.BarnUtenFolkeregisteridentifikator
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklientError
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklientError.AdressebeskyttelseKunneIkkeAvklares
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklientError.DeserializationException
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklientError.FantIkkePerson
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklientError.FødselKunneIkkeAvklares
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklientError.Ikke2xx
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklientError.IngenNavnFunnet
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklientError.NavnKunneIkkeAvklares
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklientError.NetworkError
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklientError.ResponsManglerData
import no.nav.tiltakspenger.libs.personklient.pdl.FellesPersonklientError.UkjentFeil
import no.nav.tiltakspenger.libs.personklient.pdl.dto.AdressebeskyttelseGradering
import no.nav.tiltakspenger.libs.personklient.pdl.dto.GeografiskTilknytning
import no.nav.tiltakspenger.libs.personklient.pdl.dto.PdlPerson
import no.nav.tiltakspenger.libs.personklient.pdl.dto.avklarFødsel
import no.nav.tiltakspenger.libs.personklient.pdl.dto.avklarGradering
import no.nav.tiltakspenger.libs.personklient.pdl.dto.avklarNavn
import no.nav.tiltakspenger.libs.personklient.pdl.dto.toBarnUtenforFolkeregisteret
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerBarnUtenIdent
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Kopiert fra no.nav.tiltakspenger.vedtak.routes.rivers.PersonopplysningerRoutes.kt
 */
internal fun mapPersonopplysninger(
    json: String,
    innhentet: LocalDateTime,
    fnr: Fnr,
): List<Personopplysninger> {
    val data: PdlResponseData = objectMapper.readValue<PdlResponseData>(json)
    val person: PdlPerson = data.hentPerson
    val geografiskTilknytning: GeografiskTilknytning? = data.hentGeografiskTilknytning
    return either {
        val navn = avklarNavn(person.navn).bind()
        val fødsel = avklarFødsel(person.foedsel).bind()
        val adressebeskyttelse: AdressebeskyttelseGradering = avklarGradering(person.adressebeskyttelse).bind()
        person.forelderBarnRelasjon
            .toBarnUtenforFolkeregisteret()
            .filter { it.kanGiRettPåBarnetillegg() }
            .map { barn ->
                PersonopplysningerBarnUtenIdent(
                    fødselsdato = barn.fødselsdato,
                    fornavn = barn.fornavn,
                    mellomnavn = barn.mellomnavn,
                    etternavn = barn.etternavn,
                    tidsstempelHosOss = innhentet,
                )
            } +
            PersonopplysningerSøker(
                fnr = fnr,
                fødselsdato = fødsel.foedselsdato,
                fornavn = navn.fornavn,
                mellomnavn = navn.mellomnavn,
                etternavn = navn.etternavn,
                fortrolig = adressebeskyttelse.erFortrolig(),
                strengtFortrolig = adressebeskyttelse.erStrengtFortrolig(),
                strengtFortroligUtland = adressebeskyttelse.erStrengtFortroligUtland(),
                skjermet = null,
                kommune = geografiskTilknytning?.gtKommune,
                bydel = geografiskTilknytning?.gtBydel,
                tidsstempelHosOss = innhentet,
            )
    }.getOrElse { it.mapError() }
}

private const val ALDER_BARNETILLEGG = 16L
private const val SIKKERHETSMARGIN_ÅR = 2L // søknaden sender med barn opp til 18 år. Vi lagrer det samme just in case

private fun BarnIFolkeregisteret.kanGiRettPåBarnetillegg() =
    fødselsdato.isAfter(LocalDate.now().minusYears(ALDER_BARNETILLEGG).minusYears(SIKKERHETSMARGIN_ÅR))

// TODO pre-mvp jah: Vi kan ikke bruke LocalDate.now(). Vi må sammenligne med vurderingsperioden.
private fun BarnUtenFolkeregisteridentifikator.kanGiRettPåBarnetillegg() =
    fødselsdato?.isAfter(LocalDate.now().minusYears(ALDER_BARNETILLEGG).minusYears(SIKKERHETSMARGIN_ÅR)) ?: true

private data class PdlResponseData(
    val hentGeografiskTilknytning: GeografiskTilknytning?,
    val hentPerson: PdlPerson,
)

private val objectMapper: ObjectMapper =
    JsonMapper
        .builder()
        .addModule(JavaTimeModule())
        .addModule(KotlinModule.Builder().build())
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)
        .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
        .enable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
        .build()

internal fun FellesPersonklientError.mapError(): Nothing {
    when (this) {
        is AdressebeskyttelseKunneIkkeAvklares -> throw RuntimeException(
            "Feil ved henting av personopplysninger: AdressebeskyttelseKunneIkkeAvklares",
        )
        is DeserializationException -> throw RuntimeException(
            "Feil ved henting av personopplysninger: DeserializationException",
            this.exception,
        )

        is FantIkkePerson -> throw RuntimeException("Feil ved henting av personopplysninger: FantIkkePerson")
        is FødselKunneIkkeAvklares -> throw RuntimeException("Feil ved henting av personopplysninger: FødselKunneIkkeAvklares")
        is Ikke2xx -> throw RuntimeException("Feil ved henting av personopplysninger: $this")
        is IngenNavnFunnet -> throw RuntimeException("Feil ved henting av personopplysninger: IngenNavnFunnet")
        is NavnKunneIkkeAvklares -> throw RuntimeException("Feil ved henting av personopplysninger: NavnKunneIkkeAvklares")
        is NetworkError -> throw RuntimeException(
            "Feil ved henting av personopplysninger: NetworkError",
            this.exception,
        )

        is ResponsManglerData -> throw RuntimeException("Feil ved henting av personopplysninger: ResponsManglerPerson")
        is UkjentFeil -> throw RuntimeException("Feil ved henting av personopplysninger: $this")
    }
}
