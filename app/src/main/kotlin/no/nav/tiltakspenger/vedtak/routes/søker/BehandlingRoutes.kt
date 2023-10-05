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
import no.nav.tiltakspenger.domene.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vedtak.service.behandling.BehandlingService
import no.nav.tiltakspenger.vedtak.service.sak.SakService
import no.nav.tiltakspenger.vedtak.tilgang.InnloggetSaksbehandlerProvider
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import java.time.LocalDate

private val LOG = KotlinLogging.logger {}

internal const val behandlingPath = "/behandling"

data class SammenstillingForBehandlingDTO(
    val behandlingId: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val søknad: SøknadDTO,
    val saksopplysninger: List<SaksopplysningUtDTO>,
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

data class SaksopplysningUtDTO(
    val fom: LocalDate,
    val tom: LocalDate,
    val kilde: String,
    val detaljer: String,
    val typeSaksopplysning: String,
    val vilkårTittel: String,
    val vilkårParagraf: String,
    val vilkårLedd: String,
    val fakta: String,
    val utfall: String,
)

data class SaksopplysningDTO(
    val fom: String,
    val tom: String,
    val vilkår: String,
    val begrunnelse: String,
    val harYtelse: Boolean,
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
        saksopplysninger = behandling.saksopplysninger.map {
            SaksopplysningUtDTO(
                fom = it.fom,
                tom = it.tom,
                kilde = it.kilde.navn,
                detaljer = it.detaljer,
                typeSaksopplysning = it.typeSaksopplysning.name,
                vilkårTittel = it.vilkår.tittel,
                vilkårParagraf = it.vilkår.lovreferanse.paragraf,
                vilkårLedd = it.vilkår.lovreferanse.ledd,
                fakta = faktatekst(it.vilkår, it.typeSaksopplysning),
                utfall = if (behandling is BehandlingVilkårsvurdert) behandling.vilkårsvurderinger.first { vurdering -> vurdering.vilkår == it.vilkår && vurdering.fom == it.fom }.utfall.name else Utfall.KREVER_MANUELL_VURDERING.name,
            )
        },
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

enum class Faktatekst(val harYtelse: String, val harIkkeYtelse: String) {
    AAP("Bruker mottar AAP", "Bruker mottar ikke AAP"),
    DAGPENGER("Bruker mottar dagpenger", "Bruker mottar ikke dagpenger"),
    KVP("Bruker går på KVP", "Bruker går ikke på KVP"),
}

fun faktatekst(vilkår: Vilkår, typeSaksopplysning: TypeSaksopplysning): String {
    if (typeSaksopplysning == TypeSaksopplysning.IKKE_INNHENTET_ENDA) return "Ikke innhentet"
    return when (vilkår) {
        Vilkår.AAP -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.AAP.harYtelse else Faktatekst.AAP.harIkkeYtelse
        Vilkår.ALDER -> TODO()
        Vilkår.ALDERSPENSJON -> TODO()
        Vilkår.DAGPENGER -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.DAGPENGER.harYtelse else Faktatekst.DAGPENGER.harIkkeYtelse
        Vilkår.FORELDREPENGER -> TODO()
        Vilkår.GJENLEVENDEPENSJON -> TODO()
        Vilkår.INSTITUSJONSOPPHOLD -> TODO()
        Vilkår.INTROPROGRAMMET -> TODO()
        Vilkår.KOMMUNALEYTELSER -> TODO()
        Vilkår.KVP -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.KVP.harYtelse else Faktatekst.KVP.harIkkeYtelse
        Vilkår.LØNNSINNTEKT -> TODO()
        Vilkår.OMSORGSPENGER -> TODO()
        Vilkår.OPPLÆRINGSPENGER -> TODO()
        Vilkår.OVERGANGSSTØNAD -> TODO()
        Vilkår.PENSJONSINNTEKT -> TODO()
        Vilkår.PLEIEPENGER_NÆRSTÅENDE -> TODO()
        Vilkår.PLEIEPENGER_SYKT_BARN -> TODO()
        Vilkår.STATLIGEYTELSER -> TODO()
        Vilkår.SUPPLERENDESTØNADALDER -> TODO()
        Vilkår.SUPPLERENDESTØNADFLYKTNING -> TODO()
        Vilkår.SVANGERSKAPSPENGER -> TODO()
        Vilkår.SYKEPENGER -> TODO()
        Vilkår.TILTAKSPENGER -> TODO()
        Vilkår.UFØRETRYGD -> TODO()
    }
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
        behandling.leggTilSaksopplysning(lagSaksopplysningMedVilkår(nySaksopplysning))

        call.respond(status = HttpStatusCode.OK, message = "{}")
    }
}

private fun lagSaksopplysningMedVilkår(saksopplysning: SaksopplysningDTO): Saksopplysning {
    val vilkår = when (saksopplysning.vilkår) {
        "AAP" -> Vilkår.AAP
        "DAGPENGER" -> Vilkår.DAGPENGER
        else -> throw IllegalStateException("Kan ikke lage saksopplysning for vilkår ${saksopplysning.vilkår}")
    }

    return Saksopplysning.lagSaksopplysningFraSBH(
        fom = LocalDate.parse(saksopplysning.fom),
        tom = LocalDate.parse(saksopplysning.tom),
        vilkår = vilkår,
        detaljer = saksopplysning.begrunnelse,
        typeSaksopplysning = if (saksopplysning.harYtelse) TypeSaksopplysning.HAR_YTELSE else TypeSaksopplysning.HAR_IKKE_YTELSE,
    )
}
