package no.nav.tiltakspenger.vedtak.routes.behandling

import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.saksopplysning.TypeSaksopplysning
import no.nav.tiltakspenger.vedtak.Personopplysninger
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
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
    val fakta: String,
    val utfall: String,
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

// TODO: Gå gjennom med fag og finn riktige tekster
enum class Faktatekst(val harYtelse: String, val harIkkeYtelse: String) {
    AAP("Bruker mottar AAP", "Bruker mottar ikke AAP"),
    ALDER("Bruker er over 18 år", "Bruker er under 18 år"),
    ALDERSPENSJON("Bruker mottar alderspensjon", "Bruker mottar ikke alderspensjon"),
    DAGPENGER("Bruker mottar dagpenger", "Bruker mottar ikke dagpenger"),
    FORELDREPENGER("Bruker mottar foreldrepenger", "Bruker mottar ikke foreldrepenger"),
    GJENLEVENDEPENSJON("Bruker mottar gjenlevendepensjon", "Bruker mottar ikke gjenlevendepensjon"),
    INSTITUSJONSOPPHOLD("Bruker har institusjonsopphold", "Bruker har ikke institusjonsopphold"),
    INTROPROGRAMMET("Bruker mottar introprogrammet", "Bruker mottar ikke introprogrammet"),
    JOBBSJANSEN("Bruker deltar på jobbsjansen", "Bruker deltar ikke på jobbsjansen"),
    KOMMUNALEYTELSER("Bruker mottar kommunaleytelser", "Bruker mottar ikke kommunaleytelser"),
    KVP("Bruker går på KVP", "Bruker går ikke på KVP"),
    LØNNSINNTEKT("Bruker mottar lønnsinntekt", "Bruker mottar ikke lønnsinntekt"),
    OMSORGSPENGER("Bruker mottar omsorgspenger", "Bruker mottar ikke omsorgspenger"),
    OPPLÆRINGSPENGER("Bruker mottar opplæringspenger", "Bruker mottar ikke opplæringspenger"),
    OVERGANGSSTØNAD("Bruker mottar overgangsstønad", "Bruker mottar ikke overgangsstønad"),
    PENSJONSINNTEKT("Bruker mottar pensjonsinntekt", "Bruker mottar ikke pensjonsinntekt"),
    PLEIEPENGER_NÆRSTÅENDE("Bruker mottar pleiepenger nærstående", "Bruker mottar ikke pleiepenger nærstående"),
    PLEIEPENGER_SYKT_BARN("Bruker mottar pleiepenger sykt barn", "Bruker mottar ikke pleiepenger sykt barn"),
    STATLIGEYTELSER("Bruker mottar statligeytelser", "Bruker mottar ikke statligeytelser"),
    SUPPLERENDESTØNADALDER("Bruker mottar supplerendestønadalder", "Bruker mottar ikke supplerendestønadalder"),
    SUPPLERENDESTØNADFLYKTNING(
        "Bruker mottar supplerende stønad flyktning",
        "Bruker mottar ikke supplerende stønad flyktning",
    ),
    SVANGERSKAPSPENGER("Bruker mottar svangerskapspenger", "Bruker mottar ikke svangerskapspenger"),
    SYKEPENGER("Bruker mottar sykepenger", "Bruker mottar ikke sykepenger"),
    TILTAKSPENGER("Bruker mottar tiltakspenger", "Bruker mottar ikke tiltakspenger"),
    UFØRETRYGD("Bruker mottar uføretrygd", "Bruker mottar ikke uføretrygd"),
}

fun faktatekst(vilkår: Vilkår, typeSaksopplysning: TypeSaksopplysning): String {
    if (typeSaksopplysning == TypeSaksopplysning.IKKE_INNHENTET_ENDA) return "Ikke innhentet"
    return when (vilkår) {
        Vilkår.AAP -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.AAP.harYtelse else Faktatekst.AAP.harIkkeYtelse
        Vilkår.ALDER -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.ALDER.harYtelse else Faktatekst.ALDER.harIkkeYtelse
        Vilkår.ALDERSPENSJON -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.ALDERSPENSJON.harYtelse else Faktatekst.ALDERSPENSJON.harIkkeYtelse
        Vilkår.DAGPENGER -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.DAGPENGER.harYtelse else Faktatekst.DAGPENGER.harIkkeYtelse
        Vilkår.FORELDREPENGER -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.FORELDREPENGER.harYtelse else Faktatekst.FORELDREPENGER.harIkkeYtelse
        Vilkår.GJENLEVENDEPENSJON -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.GJENLEVENDEPENSJON.harYtelse else Faktatekst.GJENLEVENDEPENSJON.harIkkeYtelse
        Vilkår.INSTITUSJONSOPPHOLD -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.INSTITUSJONSOPPHOLD.harYtelse else Faktatekst.INSTITUSJONSOPPHOLD.harIkkeYtelse
        Vilkår.INTROPROGRAMMET -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.INTROPROGRAMMET.harYtelse else Faktatekst.INTROPROGRAMMET.harIkkeYtelse
        Vilkår.JOBBSJANSEN -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.JOBBSJANSEN.harYtelse else Faktatekst.JOBBSJANSEN.harIkkeYtelse
        Vilkår.KVP -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.KVP.harYtelse else Faktatekst.KVP.harIkkeYtelse
        Vilkår.LØNNSINNTEKT -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.LØNNSINNTEKT.harYtelse else Faktatekst.LØNNSINNTEKT.harIkkeYtelse
        Vilkår.OMSORGSPENGER -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.OMSORGSPENGER.harYtelse else Faktatekst.OMSORGSPENGER.harIkkeYtelse
        Vilkår.OPPLÆRINGSPENGER -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.OPPLÆRINGSPENGER.harYtelse else Faktatekst.OPPLÆRINGSPENGER.harIkkeYtelse
        Vilkår.OVERGANGSSTØNAD -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.OVERGANGSSTØNAD.harYtelse else Faktatekst.OVERGANGSSTØNAD.harIkkeYtelse
        Vilkår.PENSJONSINNTEKT -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.PENSJONSINNTEKT.harYtelse else Faktatekst.PENSJONSINNTEKT.harIkkeYtelse
        Vilkår.PLEIEPENGER_NÆRSTÅENDE -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.PLEIEPENGER_NÆRSTÅENDE.harYtelse else Faktatekst.PLEIEPENGER_NÆRSTÅENDE.harIkkeYtelse
        Vilkår.PLEIEPENGER_SYKT_BARN -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.PLEIEPENGER_SYKT_BARN.harYtelse else Faktatekst.PLEIEPENGER_SYKT_BARN.harIkkeYtelse
        Vilkår.SUPPLERENDESTØNADALDER -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.SUPPLERENDESTØNADALDER.harYtelse else Faktatekst.SUPPLERENDESTØNADALDER.harIkkeYtelse
        Vilkår.SUPPLERENDESTØNADFLYKTNING -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.SUPPLERENDESTØNADFLYKTNING.harYtelse else Faktatekst.SUPPLERENDESTØNADFLYKTNING.harIkkeYtelse
        Vilkår.SVANGERSKAPSPENGER -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.SVANGERSKAPSPENGER.harYtelse else Faktatekst.SVANGERSKAPSPENGER.harIkkeYtelse
        Vilkår.SYKEPENGER -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.SYKEPENGER.harYtelse else Faktatekst.SYKEPENGER.harIkkeYtelse
        Vilkår.TILTAKSPENGER -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.TILTAKSPENGER.harYtelse else Faktatekst.TILTAKSPENGER.harIkkeYtelse
        Vilkår.UFØRETRYGD -> if (typeSaksopplysning == TypeSaksopplysning.HAR_YTELSE) Faktatekst.UFØRETRYGD.harYtelse else Faktatekst.UFØRETRYGD.harIkkeYtelse
        Vilkår.KOMMUNALEYTELSER -> throw IllegalArgumentException("Vi har ikke støtte for denne vilkårstypen: $vilkår")
        Vilkår.STATLIGEYTELSER -> throw IllegalArgumentException("Vi har ikke støtte for denne vilkårstypen: $vilkår")
    }
}
