package no.nav.tiltakspenger.vedtak.routes.rivers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.libs.person.AdressebeskyttelseGradering.FORTROLIG
import no.nav.tiltakspenger.libs.person.AdressebeskyttelseGradering.STRENGT_FORTROLIG
import no.nav.tiltakspenger.libs.person.AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND
import no.nav.tiltakspenger.libs.person.BarnIFolkeregisteret
import no.nav.tiltakspenger.libs.person.BarnUtenFolkeregisteridentifikator
import no.nav.tiltakspenger.libs.person.Feilmelding
import no.nav.tiltakspenger.libs.person.Person
import no.nav.tiltakspenger.libs.person.PersonRespons
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Feil
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.meldinger.FeilMottattHendelse
import no.nav.tiltakspenger.vedtak.meldinger.PersonopplysningerMottattHendelse
import no.nav.tiltakspenger.vedtak.service.sak.SakService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSystembrukerProvider
import java.time.LocalDate
import java.time.LocalDateTime

val personopplysningerPath = "/rivers/personopplysninger"

private val LOG = KotlinLogging.logger {}

data class PersonopplysningerMottattDTO(
    val journalpostId: String,
    val ident: String,
    val personopplysninger: PersonRespons,
    val innhentet: LocalDateTime,
)

fun Route.personopplysningerRoutes(
    innloggetSystembrukerProvider: InnloggetSystembrukerProvider,
    innsendingMediator: InnsendingMediator,
    søkerMediator: SøkerMediator,
    sakService: SakService,
) {
    post("$personopplysningerPath") {
        LOG.info { "Vi har mottatt personopplysninger fra river" }
        val systembruker: Systembruker = innloggetSystembrukerProvider.hentInnloggetSystembruker(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        LOG.info { "Vi ble kallt med systembruker : $systembruker" }

        val personopplysningerMottattDTO = call.receive<PersonopplysningerMottattDTO>()

        when {
            personopplysningerMottattDTO.personopplysninger.feil != null -> {
                val feilMottattHendelse = FeilMottattHendelse(
                    aktivitetslogg = Aktivitetslogg(),
                    journalpostId = personopplysningerMottattDTO.journalpostId,
                    ident = personopplysningerMottattDTO.ident,
                    feil = when (personopplysningerMottattDTO.personopplysninger.feil!!) {
                        Feilmelding.PersonIkkeFunnet -> Feil.PersonIkkeFunnet
                    },
                )
                innsendingMediator.håndter(feilMottattHendelse)
                call.respond(message = "OK", status = HttpStatusCode.OK)
            }

            personopplysningerMottattDTO.personopplysninger.person != null -> {
                val personopplysninger = mapPersonopplysninger(
                    personopplysningerMottattDTO.personopplysninger.person!!,
                    personopplysningerMottattDTO.innhentet,
                    personopplysningerMottattDTO.ident,
                )
                sakService.mottaPersonopplysninger(personopplysninger)
                val personopplysningerMottattHendelse = PersonopplysningerMottattHendelse(
                    aktivitetslogg = Aktivitetslogg(),
                    journalpostId = personopplysningerMottattDTO.journalpostId,
                    ident = personopplysningerMottattDTO.ident,
                    personopplysninger = personopplysninger,
                    tidsstempelPersonopplysningerInnhentet = personopplysningerMottattDTO.innhentet,
                )
                innsendingMediator.håndter(personopplysningerMottattHendelse)
                søkerMediator.håndter(personopplysningerMottattHendelse)
                call.respond(message = "OK", status = HttpStatusCode.OK)
            }

            else -> throw IllegalStateException("Mottatt en personopplysning som ikke har hverken person eller feil")
        }
    }
}

const val ALDER_BARNETILLEGG = 16L
const val SIKKERHETSMARGIN_ÅR = 2L // søknaden sender med barn opp til 18 år. Vi lagrer det samme just in case
fun BarnIFolkeregisteret.kanGiRettPåBarnetillegg() =
    fødselsdato.isAfter(LocalDate.now().minusYears(ALDER_BARNETILLEGG).minusYears(SIKKERHETSMARGIN_ÅR))

fun BarnUtenFolkeregisteridentifikator.kanGiRettPåBarnetillegg() =
    fødselsdato?.isAfter(LocalDate.now().minusYears(ALDER_BARNETILLEGG).minusYears(SIKKERHETSMARGIN_ÅR)) ?: true

private fun mapPersonopplysninger(
    dto: Person,
    innhentet: LocalDateTime,
    ident: String,
): List<Personopplysninger> {
    return dto.barn.filter { it.kanGiRettPåBarnetillegg() }.map {
        Personopplysninger.BarnMedIdent(
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
        Personopplysninger.BarnUtenIdent(
            fødselsdato = barn.fødselsdato,
            fornavn = barn.fornavn,
            mellomnavn = barn.mellomnavn,
            etternavn = barn.etternavn,
            tidsstempelHosOss = innhentet,
        )
    } + Personopplysninger.Søker(
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
