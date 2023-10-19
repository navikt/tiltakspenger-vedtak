package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.domene.behandling.Behandling
import no.nav.tiltakspenger.domene.behandling.BehandlingTilBeslutter
import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import java.time.LocalDate

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
    val fakta: FaktaDTO,
    val utfall: String,
)

data class FaktaDTO(
    val harYtelse: String,
    val harIkkeYtelse: String,
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
        saksopplysninger = behandling.saksopplysninger().map {
            val fakta = fakta[it.vilkår.tittel] ?: FaktaDTO(harYtelse = "ukjent", harIkkeYtelse = "ukjent")
            SaksopplysningUtDTO(
                fom = it.fom,
                tom = it.tom,
                kilde = it.kilde.navn,
                detaljer = it.detaljer,
                typeSaksopplysning = it.typeSaksopplysning.name,
                vilkårTittel = it.vilkår.tittel,
                vilkårParagraf = it.vilkår.lovreferanse.paragraf,
                vilkårLedd = it.vilkår.lovreferanse.ledd,
                fakta = fakta,
                utfall = settUtfall(behandling = behandling, saksopplysning = it),
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

fun settUtfall(behandling: Behandling, saksopplysning: Saksopplysning): String {
    return when (behandling) {
        is BehandlingVilkårsvurdert -> behandling.utfallForVilkår(saksopplysning.vilkår).name
        is BehandlingTilBeslutter -> behandling.hentUtfallForSaksopplysning(saksopplysning).name
        else -> Utfall.KREVER_MANUELL_VURDERING.name
    }
}

val fakta = hashMapOf(
    "AAP" to FaktaDTO(harYtelse = "Bruker mottar AAP", harIkkeYtelse = ""),
    "ALDER" to FaktaDTO(harYtelse = "Bruker er over 18 år", harIkkeYtelse = "Bruker er under 18 år"),
    "ALDERSPENSJON" to FaktaDTO(
        harYtelse = "Bruker mottar alderspensjon",
        harIkkeYtelse = "Bruker mottar ikke alderspensjon",
    ),
    "DAGPENGER" to FaktaDTO(harYtelse = "Bruker mottar dagpenger", harIkkeYtelse = "Bruker mottar ikke dagpenger"),
    "FORELDREPENGER" to FaktaDTO(
        harYtelse = "Bruker mottar foreldrepenger",
        harIkkeYtelse = "Bruker mottar ikke foreldrepenger",
    ),
    "GJENLEVENDEPENSJON" to FaktaDTO(
        harYtelse = "Bruker mottar gjenlevendepensjon",
        harIkkeYtelse = "Bruker mottar ikke gjenlevendepensjon",
    ),
    "INSTITUSJONSOPPHOLD" to FaktaDTO(
        harYtelse = "Bruker har institusjonsopphold",
        harIkkeYtelse = "Bruker har ikke institusjonsopphold",
    ),
    "INTROPROGRAMMET" to FaktaDTO(
        harYtelse = "Bruker mottar introprogrammet",
        harIkkeYtelse = "Bruker mottar ikke introprogrammet",
    ),
    "JOBBSJANSEN" to FaktaDTO(
        harYtelse = "Bruker deltar på jobbsjansen",
        harIkkeYtelse = "Bruker deltar ikke på jobbsjansen",
    ),
    "KOMMUNALEYTELSER" to FaktaDTO(
        harYtelse = "Bruker mottar kommunaleytelser",
        harIkkeYtelse = "Bruker mottar ikke kommunaleytelser",
    ),
    "KVP" to FaktaDTO(harYtelse = "Bruker går på KVP", harIkkeYtelse = "Bruker går ikke på KVP"),
    "LØNNSINNTEKT" to FaktaDTO(
        harYtelse = "Bruker mottar lønnsinntekt",
        harIkkeYtelse = "Bruker mottar ikke lønnsinntekt",
    ),
    "OMSORGSPENGER" to FaktaDTO(
        harYtelse = "Bruker mottar omsorgspenger",
        harIkkeYtelse = "Bruker mottar ikke omsorgspenger",
    ),
    "OPPLÆRINGSPENGER" to FaktaDTO(
        harYtelse = "Bruker mottar opplæringspenger",
        harIkkeYtelse = "Bruker mottar ikke opplæringspenger",
    ),
    "OVERGANGSSTØNAD" to FaktaDTO(
        harYtelse = "Bruker mottar overgangsstønad",
        harIkkeYtelse = "Bruker mottar ikke overgangsstønad",
    ),
    "PENSJONSINNTEKT" to FaktaDTO(
        harYtelse = "Bruker mottar pensjonsinntekt",
        harIkkeYtelse = "Bruker mottar ikke pensjonsinntekt",
    ),
    "PLEIEPENGER_NÆRSTÅENDE" to FaktaDTO(
        harYtelse = "Bruker mottar pleiepenger nærstående",
        harIkkeYtelse = "Bruker mottar ikke pleiepenger nærstående",
    ),
    "PLEIEPENGER_SYKT_BARN" to FaktaDTO(
        harYtelse = "Bruker mottar pleiepenger sykt barn",
        harIkkeYtelse = "Bruker mottar ikke pleiepenger sykt barn",
    ),
    "STATLIGEYTELSER" to FaktaDTO(
        harYtelse = "Bruker mottar statligeytelser",
        harIkkeYtelse = "Bruker mottar ikke statligeytelser",
    ),
    "SUPPLERENDESTØNADALDER" to FaktaDTO(
        harYtelse = "Bruker mottar supplerendestønadalder",
        harIkkeYtelse = "Bruker mottar ikke supplerendestønadalder",
    ),
    "SUPPLERENDESTØNADFLYKTNING" to FaktaDTO(
        harYtelse = "Bruker mottar supplerende stønad flyktning",
        harIkkeYtelse = "Bruker mottar ikke supplerende stønad flyktning",
    ),
    "SVANGERSKAPSPENGER" to FaktaDTO(
        harYtelse = "Bruker mottar svangerskapspenger",
        harIkkeYtelse = "Bruker mottar ikke svangerskapspenger",
    ),
    "SYKEPENGER" to FaktaDTO(harYtelse = "Bruker mottar sykepenger", harIkkeYtelse = "Bruker mottar ikke sykepenger"),
    "TILTAKSPENGER" to FaktaDTO(
        harYtelse = "Bruker mottar tiltakspenger",
        harIkkeYtelse = "Bruker mottar ikke tiltakspenger",
    ),
    "UFØRETRYGD" to FaktaDTO(harYtelse = "Bruker mottar uføretrygd", harIkkeYtelse = "Bruker mottar ikke uføretrygd"),
    "ETTERLØNN" to FaktaDTO(harYtelse = "Bruker mottar etterlønn", harIkkeYtelse = "Bruker mottar ikke etterlønn"),
)
