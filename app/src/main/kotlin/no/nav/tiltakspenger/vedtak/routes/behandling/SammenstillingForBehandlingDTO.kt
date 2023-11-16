package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.domene.behandling.Behandling
import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.domene.behandling.BehandlingTilBeslutter
import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.domene.behandling.Personopplysninger
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.vilkår.Utfall
import no.nav.tiltakspenger.domene.vilkår.Vilkår
import no.nav.tiltakspenger.vedtak.service.søker.PeriodeDTO
import java.time.LocalDate

data class SammenstillingForBehandlingDTO(
    val behandlingId: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val søknad: SøknadDTO,
    val registrerteTiltak: List<RegistrertTiltakDTO>,
    val saksopplysninger: List<KategoriserteSaksopplysningerDTO>,
    val personopplysninger: PersonopplysningerDTO,
    val tilstand: String,
)

data class PersonopplysningerDTO(
    val ident: String,
    val fornavn: String,
    val etternavn: String,
    val skjerming: Boolean,
    val strengtFortrolig: Boolean,
    val fortrolig: Boolean,
)

data class RegistrertTiltakDTO(
    val arrangør: String,
    val dagerIUken: Int,
    val navn: String,
    val periode: PeriodeDTO,
    val prosent: Int,
    val status: String,
)

data class SøknadDTO(
    val søknadsdato: LocalDate,
    val arrangoernavn: String,
    val tiltakstype: String,
    val deltakelseFom: LocalDate,
    val deltakelseTom: LocalDate,
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

data class KategoriserteSaksopplysningerDTO(
    val kategoriTittel: String,
    val saksopplysninger: List<SaksopplysningUtDTO>,
    val samletUtfall: String,
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
            arrangoernavn = behandling.søknad().tiltak.arrangør,
            tiltakstype = behandling.søknad().tiltak.typeNavn,
            deltakelseFom = behandling.søknad().tiltak.deltakelseFom,
            deltakelseTom = behandling.søknad().tiltak.deltakelseTom,
        ),
        registrerteTiltak = behandling.tiltak.map {
            RegistrertTiltakDTO(
                arrangør = it.gjennomføring.arrangørnavn,
                dagerIUken = it.deltakelseDagerUke?.toInt() ?: 0,
                navn = it.gjennomføring.typeNavn,
                periode = PeriodeDTO(
                    fra = it.deltakelseFom ?: LocalDate.MIN,
                    til = it.deltakelseTom ?: LocalDate.MAX,
                ),
                prosent = it.deltakelseProsent?.toInt() ?: 0,
                status = it.deltakelseStatus.status,
            )
        },
        saksopplysninger = Kategori.entries.map { kategori ->
            KategoriserteSaksopplysningerDTO(
                kategoriTittel = kategori.tittel,
                saksopplysninger = behandling.saksopplysninger().filter { kategori.vilkår.contains(it.vilkår) }.map {
                    val fakta = fakta[it.vilkår.tittel] ?: FaktaDTO(harYtelse = "ukjent", harIkkeYtelse = "ukjent")
                    SaksopplysningUtDTO(
                        fom = it.fom,
                        tom = it.tom,
                        kilde = it.kilde.navn,
                        detaljer = it.detaljer,
                        typeSaksopplysning = it.typeSaksopplysning.name,
                        vilkårTittel = it.vilkår.flateTittel,
                        vilkårParagraf = it.vilkår.lovreferanse.paragraf,
                        vilkårLedd = it.vilkår.lovreferanse.ledd,
                        fakta = fakta,
                        utfall = settUtfall(behandling = behandling, saksopplysning = it),
                    )
                },
                samletUtfall = settSamletUtfall(behandling, behandling.saksopplysninger().filter { kategori.vilkår.contains(it.vilkår) }),
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
        tilstand = when (behandling) {
            is BehandlingIverksatt -> "iverksatt"
            is BehandlingTilBeslutter -> "tilBeslutter"
            is BehandlingVilkårsvurdert -> "vilkårsvurdert"
            is Søknadsbehandling.Opprettet -> "opprettet"
        },
    )
}

fun settSamletUtfall(behandling: Behandling, saksopplysninger: List<Saksopplysning>): String {
    for (saksopplysning in saksopplysninger) {
        return if (settUtfall(behandling, saksopplysning) == Utfall.KREVER_MANUELL_VURDERING.name) {
            Utfall.KREVER_MANUELL_VURDERING.name
        } else if (settUtfall(behandling, saksopplysning) == Utfall.IKKE_OPPFYLT.name) {
            Utfall.IKKE_OPPFYLT.name
        } else {
            continue
        }
    }
    return Utfall.OPPFYLT.name
}

fun settUtfall(behandling: Behandling, saksopplysning: Saksopplysning): String {
    return when (behandling) {
        is BehandlingVilkårsvurdert -> behandling.hentUtfallForVilkår(saksopplysning.vilkår).name
        is BehandlingTilBeslutter -> behandling.hentUtfallForVilkår(saksopplysning.vilkår).name
        else -> Utfall.KREVER_MANUELL_VURDERING.name
    }
}

val fakta = hashMapOf(
    "AAP" to FaktaDTO(
        harYtelse = "Bruker mottar AAP",
        harIkkeYtelse = "Bruker mottar ikke AAP",
    ),
    "ALDER" to FaktaDTO(
        harYtelse = "Bruker er under 18 år",
        harIkkeYtelse = "Bruker er over 18 år",
    ),
    "ALDERSPENSJON" to FaktaDTO(
        harYtelse = "Bruker mottar alderspensjon",
        harIkkeYtelse = "Bruker mottar ikke alderspensjon",
    ),
    "DAGPENGER" to FaktaDTO(
        harYtelse = "Bruker mottar dagpenger",
        harIkkeYtelse = "Bruker mottar ikke dagpenger",
    ),
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
    "KVP" to FaktaDTO(
        harYtelse = "Bruker går på KVP",
        harIkkeYtelse = "Bruker går ikke på KVP",
    ),
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
    "SYKEPENGER" to FaktaDTO(
        harYtelse = "Bruker mottar sykepenger",
        harIkkeYtelse = "Bruker mottar ikke sykepenger",
    ),
    "TILTAKSPENGER" to FaktaDTO(
        harYtelse = "Bruker mottar tiltakspenger",
        harIkkeYtelse = "Bruker mottar ikke tiltakspenger",
    ),
    "UFØRETRYGD" to FaktaDTO(
        harYtelse = "Bruker mottar uføretrygd",
        harIkkeYtelse = "Bruker mottar ikke uføretrygd",
    ),
    "ETTERLØNN" to FaktaDTO(
        harYtelse = "Bruker mottar etterlønn",
        harIkkeYtelse = "Bruker mottar ikke etterlønn",
    ),
)

enum class Kategori(val tittel: String, val vilkår: List<Vilkår>) {
    ALDER("Alder", listOf(Vilkår.ALDER)),
    TILTAK("Tiltak", listOf(Vilkår.TILTAKSPENGER)),
    INTROKVP("Introduksjonsprogrammet og Kvalifikasjonsprogrammet", listOf(Vilkår.INTROPROGRAMMET, Vilkår.KVP)),
    UTBETALINGER(
        "Utbetalinger",
        listOf(
            Vilkår.FORELDREPENGER,
            Vilkår.PLEIEPENGER_SYKT_BARN,
            Vilkår.PLEIEPENGER_NÆRSTÅENDE,
            Vilkår.ALDERSPENSJON,
            Vilkår.PENSJONSINNTEKT,
            Vilkår.ETTERLØNN,
            Vilkår.AAP,
            Vilkår.DAGPENGER,
            Vilkår.GJENLEVENDEPENSJON,
            Vilkår.FORELDREPENGER,
            Vilkår.JOBBSJANSEN,
            Vilkår.UFØRETRYGD,
            Vilkår.OMSORGSPENGER,
            Vilkår.OPPLÆRINGSPENGER,
            Vilkår.OVERGANGSSTØNAD,
            Vilkår.SYKEPENGER,
            Vilkår.SVANGERSKAPSPENGER,
            Vilkår.SUPPLERENDESTØNADFLYKTNING,
            Vilkår.STATLIGEYTELSER,
            Vilkår.KOMMUNALEYTELSER,

        ),
    ),
    INSTITUSJONSOPPHOLD("Institusjonsopphold", listOf(Vilkår.INSTITUSJONSOPPHOLD)),
}
