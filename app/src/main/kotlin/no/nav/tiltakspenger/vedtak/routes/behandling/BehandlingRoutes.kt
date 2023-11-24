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
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.behandling.harTilgang
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.innsending.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.innsending.meldinger.InnsendingUtdatertHendelse
import no.nav.tiltakspenger.vedtak.routes.behandling.SaksopplysningDTO.Companion.lagSaksopplysningMedVilkår
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.service.sak.SakService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val SECURELOG = KotlinLogging.logger("tjenestekall")

internal const val behandlingPath = "/behandling"
internal const val behandlingerPath = "/behandlinger"

fun Route.behandlingRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
    sakService: SakService,
    innsendingMediator: InnsendingMediator,
) {
    get("$behandlingPath/{behandlingId}") {
        SECURELOG.debug("Mottatt request på $behandlingPath/behandlingId")
        val saksbehandler: Saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@get call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@get call.respond(message = "Behandling ikke funnet", status = HttpStatusCode.NotFound)

        val sak = sakService.henteMedBehandlingsId(behandlingId) ?: return@get call.respond(
            message = "Sak ikke funnet",
            status = HttpStatusCode.NotFound,
        )

        if (sak.personopplysninger.isEmpty()) {
            return@get call.respond(
                message = "Sak mangler personopplysninger",
                status = HttpStatusCode.NotFound,
            )
        }

        if (!sak.personopplysninger.harTilgang(saksbehandler)) {
            call.respond(
                status = HttpStatusCode.Unauthorized,
                message = "{}",
            )
        }

        val behandling = sak.behandlinger.filterIsInstance<Søknadsbehandling>().firstOrNull {
            it.id == behandlingId
        } ?: return@get call.respond(message = "Behandling ikke funnet", status = HttpStatusCode.NotFound)

        // her burde vi nok ikke bare hente den første, men finne den riktige og evnt feilmelding hvis vi ikke finner den
        // val behandling = behandlingService.hentBehandling(behandlingId) Skal vi hente behandling direkte eller via sak?

        val dto = mapSammenstillingDTO(
            behandling = behandling,
            personopplysninger = sak.personopplysninger,
        )
        call.respond(status = HttpStatusCode.OK, dto)
    }

    post("$behandlingPath/{behandlingId}") {
        SECURELOG.debug("Mottatt request på $behandlingPath/")
        val saksbehandler: Saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        val nySaksopplysning = call.receive<SaksopplysningDTO>()
        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@post call.respond(message = "Behandling ikke funnet", status = HttpStatusCode.NotFound)

        behandlingService.leggTilSaksopplysning(
            behandlingId,
            lagSaksopplysningMedVilkår(saksbehandler.navIdent, nySaksopplysning),
        )

        call.respond(status = HttpStatusCode.OK, message = "{}")
    }

    post("$behandlingPath/beslutter/{behandlingId}") {
        SECURELOG.debug("Mottatt request. $behandlingPath/ skal sendes til beslutter")
        val saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        check(saksbehandler.roller.contains(Rolle.SAKSBEHANDLER)) { "Saksbehandler må være saksbehandler" }

        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@post call.respond(message = "Fant ingen behandlingId i body", status = HttpStatusCode.NotFound)

        behandlingService.sendTilBeslutter(behandlingId, saksbehandler.navIdent)

        call.respond(status = HttpStatusCode.OK, message = "{}")
    }

    post("$behandlingPath/oppdater/{behandlingId}") {
        SECURELOG.debug { "Vi har mottatt melding om oppfriskning av fakta" }
        val saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        // TODO: Rollesjekk ikke helt landet

        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@post call.respond(message = "BehandlingId ikke funnet", status = HttpStatusCode.NotFound)

        SECURELOG.info { "Saksbehandler $saksbehandler ba om oppdatering av saksopplysninger for behandling $behandlingId" }

        behandlingService.hentBehandling(behandlingId)?.let {
            val innsendingUtdatertHendelse = InnsendingUtdatertHendelse(
                aktivitetslogg = Aktivitetslogg(),
                journalpostId = it.søknad().journalpostId,
            )
            innsendingMediator.håndter(innsendingUtdatertHendelse)
        } ?: return@post call.respond(message = "Behandling ikke funnet", status = HttpStatusCode.NotFound)

        // Skriv denne om til en sjekk på om det faktisk er oppdatert
        delay(3000)
        call.respond(message = "{}", status = HttpStatusCode.OK)
    }

    post("$behandlingPath/avbrytbehandling/{behandlingId}") {
        SECURELOG.debug { "Mottatt request om å fjerne saksbehandler på behandlingen" }

        val saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        val isAdmin = saksbehandler.roller.contains(Rolle.ADMINISTRATOR)

        check(saksbehandler.roller.contains(Rolle.SAKSBEHANDLER) || isAdmin) { "Saksbehandler må være saksbehandler eller administrator" }

        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@post call.respond(message = "BehandlingId ikke funnet", status = HttpStatusCode.NotFound)

        behandlingService.avbrytBehandling(behandlingId, saksbehandler.navIdent, isAdmin)

        call.respond(message = "{}", status = HttpStatusCode.OK)
    }
}
