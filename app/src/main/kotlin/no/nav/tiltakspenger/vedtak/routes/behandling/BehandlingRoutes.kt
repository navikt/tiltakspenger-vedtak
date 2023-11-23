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
import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.domene.behandling.BehandlingTilBeslutter
import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.InnsendingMediator
import no.nav.tiltakspenger.vedtak.innsending.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.innsending.meldinger.InnsendingUtdatertHendelse
import no.nav.tiltakspenger.vedtak.routes.behandling.SaksopplysningDTO.Companion.lagSaksopplysningMedVilkår
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.service.sak.SakService
import no.nav.tiltakspenger.vedtak.service.utbetaling.UtbetalingServiceImpl
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider

private val LOG = KotlinLogging.logger {}

internal const val behandlingPath = "/behandling"
internal const val behandlingerPath = "/behandlinger"

fun Route.behandlingRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
    sakService: SakService,
    utbetalingService: UtbetalingServiceImpl,
    innsendingMediator: InnsendingMediator,
) {
    get("$behandlingPath/{behandlingId}") {
        LOG.debug("Mottatt request på $behandlingPath/behandlingId")
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

    get(behandlingerPath) {
        LOG.debug("Mottatt request på $behandlingerPath")
        val behandlinger = behandlingService.hentAlleBehandlinger().map {
            BehandlingDTO(
                id = it.id.toString(),
                ident = it.søknad().personopplysninger.ident,
                saksbehandler = it.saksbehandler,
                beslutter = when (it) {
                    is BehandlingIverksatt -> it.beslutter
                    is BehandlingTilBeslutter -> it.beslutter
                    else -> null
                },
                status = finnStatus(it),
            )
        }.sortedBy { it.id }

        call.respond(status = HttpStatusCode.OK, behandlinger)
    }

    post("$behandlingPath/{behandlingId}") {
        LOG.debug("Mottatt request på $behandlingPath/")
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
        LOG.debug("Mottatt request. $behandlingPath/ skal sendes til beslutter")
        val saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        check(saksbehandler.roller.contains(Rolle.SAKSBEHANDLER)) { "Saksbehandler må være saksbehandler" }

        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@post call.respond(message = "Fant ingen behandlingId i body", status = HttpStatusCode.NotFound)

        behandlingService.sendTilBeslutter(behandlingId, saksbehandler.navIdent)

        call.respond(status = HttpStatusCode.OK, message = "{}")
    }

    post("$behandlingPath/sendtilbake/{behandlingId}") {
        LOG.debug("Mottatt request. $behandlingPath/ send tilbake til saksbehandler")

        val saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        check(saksbehandler.roller.contains(Rolle.BESLUTTER) || saksbehandler.roller.contains(Rolle.ADMINISTRATOR)) { "Saksbehandler må være beslutter eller administrator" }

        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@post call.respond(message = "Fant ingen behandlingId i body", status = HttpStatusCode.NotFound)

        // TODO her må vi få inn begrunnelse
        behandlingService.sendTilbakeTilSaksbehandler(behandlingId, saksbehandler.navIdent, "Ikke godkjent")

        call.respond(status = HttpStatusCode.OK, message = "{}")
    }

    post("$behandlingPath/oppdater/{behandlingId}") {
        LOG.debug { "Vi har mottatt melding om oppfriskning av fakta" }
        val saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        // TODO: Rollesjekk ikke helt landet

        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@post call.respond(message = "BehandlingId ikke funnet", status = HttpStatusCode.NotFound)

        LOG.info { "Saksbehandler $saksbehandler ba om oppdatering av saksopplysninger for behandling $behandlingId" }

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

    post("$behandlingPath/startbehandling/{behandlingId}") {
        LOG.debug { "Mottatt request om å sette saksbehandler på behandlingen" }

        val saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        check(saksbehandler.roller.contains(Rolle.SAKSBEHANDLER) || saksbehandler.roller.contains(Rolle.BESLUTTER)) { "Saksbehandler må være saksbehandler eller beslutter" }

        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@post call.respond(message = "BehandlingId ikke funnet", status = HttpStatusCode.NotFound)

        behandlingService.startBehandling(behandlingId, saksbehandler.navIdent) // Bør kanskje sjekke rolle dypere

        call.respond(message = "{}", status = HttpStatusCode.OK)
    }

    post("$behandlingPath/avbrytbehandling/{behandlingId}") {
        LOG.debug { "Mottatt request om å fjerne saksbehandler på behandlingen" }

        val saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        check(saksbehandler.roller.contains(Rolle.SAKSBEHANDLER) || saksbehandler.roller.contains(Rolle.ADMINISTRATOR)) { "Saksbehandler må være saksbehandler eller administrator" }

        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@post call.respond(message = "BehandlingId ikke funnet", status = HttpStatusCode.NotFound)

        behandlingService.avbrytBehandling(behandlingId, saksbehandler.navIdent)

        call.respond(message = "{}", status = HttpStatusCode.OK)
    }

    post("$behandlingPath/godkjenn/{behandlingId}") {
        LOG.debug { "Mottat request om å godkjenne behandlingen og opprette vedtak" }

        val saksbehandler = innloggetSaksbehandlerProvider.hentInnloggetSaksbehandler(call)
            ?: return@post call.respond(message = "JWTToken ikke funnet", status = HttpStatusCode.Unauthorized)

        check(saksbehandler.roller.contains(Rolle.BESLUTTER)) { "Saksbehandler må være beslutter" }

        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@post call.respond(message = "BehandlingId ikke funnet", status = HttpStatusCode.NotFound)

        behandlingService.iverksett(behandlingId, saksbehandler.navIdent)
        call.respond(message = "{}", status = HttpStatusCode.OK)
    }

    post("$behandlingPath/sendtilutbetaling/{behandlingId}") {
        LOG.debug { "Mottat request om å godkjenne behandlingen og opprette vedtak" }

        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@post call.respond(message = "BehandlingId ikke funnet", status = HttpStatusCode.NotFound)

        val behandling = behandlingService.hentBehandling(behandlingId)
            ?: return@post call.respond(message = "Behandling ikke funnet", status = HttpStatusCode.NotFound)

        utbetalingService.sendBehandlingTilUtbetaling(behandling)

        call.respond(message = "{}", status = HttpStatusCode.OK)
    }
}

private fun finnStatus(behandling: Søknadsbehandling) =
    when (behandling) {
        is BehandlingIverksatt.Avslag -> "Iverksatt Avslag"
        is BehandlingIverksatt.Innvilget -> "Iverksatt Innvilget"
        is BehandlingTilBeslutter -> if (behandling.beslutter == null) "Klar til beslutning" else "Under beslutning"
        is BehandlingVilkårsvurdert -> if (behandling.saksbehandler == null) "Klar til behandling" else "Under behandling"
        is Søknadsbehandling.Opprettet -> "Klar til behandling"
    }
