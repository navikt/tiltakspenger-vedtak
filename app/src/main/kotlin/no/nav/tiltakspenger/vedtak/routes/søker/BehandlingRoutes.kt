package no.nav.tiltakspenger.vedtak.routes.søker

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import mu.KotlinLogging
import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.saksopplysning.SaksopplysningDTO
import no.nav.tiltakspenger.domene.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.service.sak.SakService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import java.time.LocalDate

private val LOG = KotlinLogging.logger {}

internal const val behandlingPath = "/behandling"

data class SammenstillingForBehandlingDTO(
    val behandlingId: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val søknad: SøknadDTO,
    val saksopplysninger: List<Saksopplysning>,
    val vurderinger: List<Vurdering>,
    val personopplysninger: PersonopplysningerDTO,
)

data class PersonopplysningerDTO(
    val ident: String,
    val fornavn: String,
    val etternavn: String,
    val skjerming: Boolean,
    val strengtFortrolig: Boolean,
    val fortrolig: Boolean,
)

data class SøknadDTO(
    val søknadsdato: LocalDate,
    val arrangoernavn: String,
    val tiltakstype: String,
    val startdato: LocalDate,
    val sluttdato: LocalDate,
    val antallDager: Int,
)

fun mapSammenstillingDTO(
    behandling: Søknadsbehandling,
    personopplysninger: List<Personopplysninger>,
): SammenstillingForBehandlingDTO {
    return SammenstillingForBehandlingDTO(
        behandlingId = behandling.id.toString(),
        fom = behandling.vurderingsperiode.fra,
        tom = behandling.vurderingsperiode.til,
        søknad = SøknadDTO(
            søknadsdato = behandling.søknad().opprettet.toLocalDate(),
            arrangoernavn = behandling.søknad().tiltak?.arrangoernavn!!,
            tiltakstype = behandling.søknad().tiltak?.tiltakskode?.navn!!,
            startdato = behandling.søknad().tiltak?.startdato!!,
            sluttdato = behandling.søknad().tiltak?.sluttdato!!,
            antallDager = 2,
        ),
        saksopplysninger = behandling.saksopplysninger,
        vurderinger = if (behandling is BehandlingVilkårsvurdert) behandling.vilkårsvurderinger else emptyList(),
        personopplysninger = personopplysninger.filterIsInstance<Personopplysninger.Søker>().map {
            PersonopplysningerDTO(
                ident = it.ident,
                fornavn = it.fornavn,
                etternavn = it.etternavn,
                skjerming = it.avklartSkjerming(),
                strengtFortrolig = it.strengtFortrolig,
                fortrolig = it.fortrolig,
            )
        }.first(),
    )
}

fun Route.behandlingRoutes(
    innloggetSaksbehandlerProvider: InnloggetSaksbehandlerProvider,
    behandlingService: BehandlingService,
    sakService: SakService,
) {
    get("$behandlingPath/{behandlingId}") {
        LOG.debug("Mottatt request på $behandlingPath/behandlingId")
        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@get call.respond(message = "Behandling ikke funnet", status = HttpStatusCode.NotFound)
        val sak = sakService.henteMedBehandlingsId(behandlingId)

        val behandling = sak.behandlinger.first() //  .find { it.id == behandlingId }
//        val behandling = behandlingService.hentBehandling(behandlingId)
        println("Behandling vi har hentet : $behandling")
        if (behandling !is Søknadsbehandling) throw IllegalStateException("Kan foreløpig bare hente Søknadsbehandlinger")
        val dto = mapSammenstillingDTO(
            behandling = behandling,
            personopplysninger = sak.personopplysninger,
        )
        call.respond(status = HttpStatusCode.OK, dto)
    }

    post("$behandlingPath/{behandlingId}") {
        LOG.debug("Mottatt request på $behandlingPath/")
        val nySaksopplysning = call.receive<SaksopplysningDTO>()
        val behandlingId = call.parameters["behandlingId"]?.let { BehandlingId.fromDb(it) }
            ?: return@post call.respond(message = "Behandling ikke funnet", status = HttpStatusCode.NotFound)
        val behandling = behandlingService.hentBehandling(behandlingId)
        val saksopplysning = when (nySaksopplysning.vilkårstittel) {
            "AAP" -> Saksopplysning.Aap.lagSaksopplysningFraSBH(
                fom = nySaksopplysning.fom,
                tom = nySaksopplysning.tom,
                detaljer = nySaksopplysning.begrunnelse,
                typeSaksopplysning = if (nySaksopplysning.harYtelse) TypeSaksopplysning.HAR_YTELSE else TypeSaksopplysning.HAR_IKKE_YTELSE,
            )

            else -> null
        }
        if (saksopplysning != null) behandling.leggTilSaksopplysning(saksopplysning)

        call.respond(status = HttpStatusCode.OK, "Saksopplysning ble lagret i behandlingen")
    }
}
