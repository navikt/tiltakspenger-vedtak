package no.nav.tiltakspenger.vedtak.routes.behandling

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.coroutines.delay
import mu.KotlinLogging
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.innsending.domene.Aktivitetslogg
import no.nav.tiltakspenger.innsending.domene.meldinger.InnsendingUtdatertHendelse
import no.nav.tiltakspenger.innsending.ports.InnsendingMediator
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.ports.AttesteringRepo
import no.nav.tiltakspenger.saksbehandling.service.behandling.BehandlingService
import no.nav.tiltakspenger.saksbehandling.service.sak.SakService
import no.nav.tiltakspenger.vedtak.routes.behandling.SaksopplysningDTOMapper.lagSaksopplysningMedVilkår
import no.nav.tiltakspenger.vedtak.routes.behandling.SammenstillingForBehandlingDTOMapper.mapSammenstillingDTO
import no.nav.tiltakspenger.vedtak.routes.parameter
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val SECURELOG = KotlinLogging.logger("tjenestekall")
private val LOG = KotlinLogging.logger {}

internal const val behandlingPath = "/behandling"
internal const val behandlingerPath = "/behandlinger"

data class IdentDTO(
    val ident: String?,
)

fun Route.behandlingRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
    sakService: SakService,
    innsendingMediator: InnsendingMediator,
    attesteringRepo: AttesteringRepo,
) {
    get("$behandlingPath/{behandlingId}") {
        SECURELOG.debug("Mottatt request på $behandlingPath/behandlingId")
        val saksbehandler: Saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        val sak = sakService.hentMedBehandlingId(behandlingId, saksbehandler)

        if (sak.personopplysninger.erTom()) {
            return@get call.respond(
                message = "Sak mangler personopplysninger",
                status = HttpStatusCode.NotFound,
            )
        }

        val behandling = sak.behandlinger.filterIsInstance<Førstegangsbehandling>().firstOrNull {
            it.id == behandlingId
        } ?: return@get call.respond(message = "Behandling ikke funnet", status = HttpStatusCode.NotFound)

        // her burde vi nok ikke bare hente den første, men finne den riktige og evnt feilmelding hvis vi ikke finner den
        // val behandling = behandlingService.hentBehandling(behandlingId) Skal vi hente behandling direkte eller via sak?

        val attesteringer = attesteringRepo.hentForBehandling(behandling.id)

        val dto = mapSammenstillingDTO(
            behandling = behandling,
            personopplysninger = sak.personopplysninger.søkere(),
            attesteringer = attesteringer,
        )
        call.respond(status = HttpStatusCode.OK, dto)
    }

    post("$behandlingPath/{behandlingId}") {
        SECURELOG.debug("Mottatt request på $behandlingPath/")

        val saksbehandler: Saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))
        val nySaksopplysning = call.receive<SaksopplysningDTO>()

        behandlingService.leggTilSaksopplysning(
            behandlingId,
            lagSaksopplysningMedVilkår(saksbehandler, nySaksopplysning),
        )

        call.respond(status = HttpStatusCode.OK, message = "{}")
    }

    post("$behandlingPath/beslutter/{behandlingId}") {
        SECURELOG.debug("Mottatt request. $behandlingPath/ skal sendes til beslutter")

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        behandlingService.sendTilBeslutter(behandlingId, saksbehandler)

        call.respond(status = HttpStatusCode.OK, message = "{}")
    }

    post("$behandlingPath/oppdater/{behandlingId}") {
        SECURELOG.debug { "Vi har mottatt melding om oppfriskning av fakta" }

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        SECURELOG.info { "Saksbehandler $saksbehandler ba om oppdatering av saksopplysninger for behandling $behandlingId" }

        // TODO: Rollesjekk ikke helt landet
        behandlingService.hentBehandling(behandlingId).let {
            val innsendingUtdatertHendelse = InnsendingUtdatertHendelse(
                aktivitetslogg = Aktivitetslogg(),
                journalpostId = it.søknad().journalpostId,
            )
            innsendingMediator.håndter(innsendingUtdatertHendelse)
        }

        // TODO: Skriv denne om til en sjekk på om det faktisk er oppdatert
        delay(3000)
        call.respond(message = "{}", status = HttpStatusCode.OK)
    }

    post("$behandlingPath/avbrytbehandling/{behandlingId}") {
        SECURELOG.debug { "Mottatt request om å fjerne saksbehandler på behandlingen" }

        val saksbehandler = innloggetSaksbehandlerProvider.krevInnloggetSaksbehandler(call)
        val behandlingId = BehandlingId.fromString(call.parameter("behandlingId"))

        behandlingService.frataBehandling(behandlingId, saksbehandler)

        call.respond(message = "{}", status = HttpStatusCode.OK)
    }

    post("$behandlingPath/opprettrevurdering/{saksnummer}") {
        val saksnummer = call.parameters["saksnummer"]
            ?: return@post call.respond(message = "Sak ikke funnet", status = HttpStatusCode.NotFound)

        LOG.info { "Mottatt request om å opprette en revurdering på sak med saksnummer: $saksnummer" }

        val saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        val sak = sakService.hentForSaksnummer(saksnummer = saksnummer, saksbehandler = saksbehandler)
        val sisteBehandlingId = sak.vedtak.maxBy { it.vedtaksdato }.behandling.id
        val revurdering = behandlingService.opprettRevurdering(sisteBehandlingId, saksbehandler)

        call.respond(message = "{ \"id\":\"${revurdering.id}\"}", status = HttpStatusCode.OK)
    }
}
