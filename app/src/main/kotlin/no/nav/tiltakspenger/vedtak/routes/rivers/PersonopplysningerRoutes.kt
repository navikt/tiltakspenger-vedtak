package no.nav.tiltakspenger.vedtak.routes.rivers

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vedtak.SøkerMediator
import no.nav.tiltakspenger.vedtak.meldinger.PersonopplysningerMottattHendelse
import no.nav.tiltakspenger.vedtak.rivers.AdressebeskyttelseGradering
import no.nav.tiltakspenger.vedtak.rivers.PersonopplysningerDTO
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSystembrukerProvider
import java.time.LocalDateTime

val personopplysningerPath = "/rivers/personopplysninger"

private val LOG = KotlinLogging.logger {}

data class PersonopplysningerMottattDTO(
    val journalpostId: String,
    val ident: String,
    val personopplysninger: PersonopplysningerDTO,
    val innhentet: LocalDateTime,
)

fun Route.personopplysningerRoutes(innloggetSystembrukerProvider: InnloggetSystembrukerProvider, innsendingMediator: InnsendingMediator, søkerMediator: SøkerMediator) {
    post("$personopplysningerPath") {
        LOG.info { "Vi har mottatt personopplysninger fra river" }
        val systembruker: Systembruker = innloggetSystembrukerProvider.hentInnloggetSystembruker(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        LOG.info { "Vi ble kallt med systembruker : $systembruker" }

        val personopplysninger = call.receive<PersonopplysningerMottattDTO>()
        val peronopplysningerMottattHendelse = PersonopplysningerMottattHendelse(
            aktivitetslogg = Aktivitetslogg(),
            journalpostId = personopplysninger.journalpostId,
            ident = personopplysninger.ident,
            personopplysninger = mapPersonopplysninger(
                personopplysninger.personopplysninger,
                personopplysninger.innhentet,
                personopplysninger.ident
            )
        )
        innsendingMediator.håndter(peronopplysningerMottattHendelse)
        søkerMediator.håndter(peronopplysningerMottattHendelse)
        call.respond(message = "OK", status = HttpStatusCode.OK)
    }
}

private fun mapPersonopplysninger(
    dto: PersonopplysningerDTO,
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
            fortrolig = it.adressebeskyttelseGradering == AdressebeskyttelseGradering.FORTROLIG,
            strengtFortrolig = it.adressebeskyttelseGradering == AdressebeskyttelseGradering.STRENGT_FORTROLIG,
            strengtFortroligUtland = dto.adressebeskyttelseGradering == AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND,
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
        fortrolig = dto.adressebeskyttelseGradering == AdressebeskyttelseGradering.FORTROLIG,
        strengtFortrolig = dto.adressebeskyttelseGradering == AdressebeskyttelseGradering.STRENGT_FORTROLIG,
        strengtFortroligUtland = dto.adressebeskyttelseGradering == AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND,
        skjermet = null,
        kommune = dto.gtKommune,
        bydel = dto.gtBydel,
        tidsstempelHosOss = innhentet,
    )
}
