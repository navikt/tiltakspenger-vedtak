package no.nav.tiltakspenger.vedtak.service.søknad

import no.nav.tiltakspenger.felles.Saksbehandler
import java.time.LocalDate

interface SøknadService {

    fun hentBehandlingAvSøknad(søknadId: String, saksbehandler: Saksbehandler): BehandlingDTO?
}

data class BehandlingDTO(
    val personopplysninger: PersonopplysningerDTO,
    val søknad: SøknadDTO,
    val registrerteTiltak: List<TiltakDTO>,
    val vurderingsperiode: PeriodeDTO,
    val statligeYtelser: VilkårsVurderingsKategoriDTO,
    val kommunaleYtelser: KommunaleVilkårsVurderingsKategoriDTO,
    val pensjonsordninger: VilkårsVurderingsKategoriDTO,
    val lønnsinntekt: VilkårsVurderingsKategoriDTO,
    val institusjonsopphold: VilkårsVurderingsKategoriDTO,
    val barnetillegg: List<BarnetilleggDTO>
)

data class BarnetilleggDTO(
    val navn: String,
    val alder: Int,
    val bosatt: String,
    val kilde: String,
    val utfall: UtfallDTO,
    val søktBarnetillegg: Boolean
)

data class SøknadDTO(
    val søknadId: String,
    val søknadsdato: LocalDate,
    val arrangoernavn: String?,
    val tiltakskode: String?,
    val beskrivelse: String?,
    val startdato: LocalDate,
    val sluttdato: LocalDate?,
    val antallDager: Int?,
)

data class PeriodeDTO(
    val fra: LocalDate,
    val til: LocalDate?,
)

data class TiltakDTO(
    val arrangør: String?,
    val navn: String,
    val periode: PeriodeDTO?,
    val prosent: Float?,
    val dagerIUken: Float?,
    val status: String,
)

data class VilkårsVurderingsKategoriDTO(
    val ytelse: String,
    val lovreferanse: String,
    val utfall: UtfallDTO,
    val detaljer: String,
    val vilkårsvurderinger: List<VilkårsvurderingDTO>,
)

data class KommunaleVilkårsVurderingsKategoriDTO(
    val ytelse: String,
    val lovreferanse: String,
    val utfall: UtfallDTO,
    val detaljer: String,
    val introProgrammet: List<VilkårsvurderingDTO>,
    val kvp: List<VilkårsvurderingDTO>,
)

data class VilkårsvurderingDTO(
    val ytelse: String,
    val lovreferanse: String,
    val utfall: UtfallDTO,
    val detaljer: String,
    val periode: PeriodeDTO?,
    val kilde: String,
    val vilkår: String,
)

enum class UtfallDTO {
    Oppfylt,
    Uavklart,
    IkkeOppfylt,
    KreverManuellVurdering,
    IkkeImplementert
}

data class PersonopplysningerDTO(
    val fornavn: String?,
    val etternavn: String?,
    val ident: String,
    val barn: List<BarnDTO>,
)

data class BarnDTO(
    val fornavn: String,
    val etternavn: String,
    val ident: String?,
    val bosted: String?
)
