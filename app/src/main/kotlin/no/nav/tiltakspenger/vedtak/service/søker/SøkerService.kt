package no.nav.tiltakspenger.vedtak.service.søker

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.SøkerId
import java.time.LocalDate

interface SøkerService {
    fun hentSøkerId(ident: String, saksbehandler: Saksbehandler): SøkerIdDTO?
    fun hentSøkerOgSøknader(søkerId: SøkerId, saksbehandler: Saksbehandler): SøkerDTO?
}

data class SøkerIdDTO(
    val id: String
)

data class SøkerDTO(
    val ident: String,
    val behandlinger: List<BehandlingDTO>,
    val personopplysninger: PersonopplysningerDTO?
)

data class BehandlingDTO(
    val søknad: SøknadDTO,
    val registrerteTiltak: List<TiltakDTO>,
    val vurderingsperiode: PeriodeDTO,
    val statligeYtelser: StatligeYtelserDTO,
    val kommunaleYtelser: KommunaleYtelserDTO,
    val pensjonsordninger: PensjonsordningerDTO,
    val lønnsinntekt: LønnsinntekterDTO,
    val institusjonsopphold: InstitusjonsoppholdDTO,
    val barnetillegg: List<BarnetilleggDTO>
)

data class StatligeYtelserDTO(
    val samletUtfall: UtfallDTO,
    val aap: List<VilkårsvurderingDTO>,
    val dagpenger: List<VilkårsvurderingDTO>,
)

data class VilkårsvurderingDTO(
    val kilde: String,
    val detaljer: String,
    val periode: PeriodeDTO?,
    val kreverManuellVurdering: Boolean,
    val utfall: UtfallDTO,
)


data class KommunaleYtelserDTO(
    val samletUtfall: UtfallDTO,
    val kvp: List<VilkårsvurderingDTO>,
    val introProgrammet: List<VilkårsvurderingDTO>
)

data class PensjonsordningerDTO(
    val samletUtfall: UtfallDTO,
    val perioder: List<VilkårsvurderingDTO>
)

data class LønnsinntekterDTO(
    val samletUtfall: UtfallDTO,
    val perioder: List<VilkårsvurderingDTO>
)

data class InstitusjonsoppholdDTO(
    val samletUtfall: UtfallDTO,
    val perioder: List<VilkårsvurderingDTO>
)

data class BarnetilleggDTO(
    val navn: String?,
    val alder: Int,
    val fødselsdato: LocalDate?,
    val bosatt: String,
    val kilde: String,
    val utfall: UtfallDTO,
    val søktBarnetillegg: Boolean
)

data class SøknadDTO(
    val id: String,
    val søknadId: String,
    val søknadsdato: LocalDate,
    val arrangoernavn: String?,
    val tiltakskode: String?,
    val beskrivelse: String?,
    val startdato: LocalDate,
    val sluttdato: LocalDate?,
    val antallDager: Int?,
    val fritekst: String?,
    val vedlegg: List<VedleggDTO>
)

data class VedleggDTO(
    val journalpostId: String,
    val dokumentInfoId: String,
    val filnavn: String?,
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
    val fortrolig: Boolean,
    val strengtFortrolig: Boolean,
    val skjermet: Boolean,
)

data class BarnDTO(
    val fornavn: String,
    val etternavn: String,
    val ident: String?,
    val bosted: String?
)
