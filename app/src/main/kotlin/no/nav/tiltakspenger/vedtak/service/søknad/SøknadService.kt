package no.nav.tiltakspenger.vedtak.service.søknad

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.SøknadId
import java.time.LocalDate

interface SøknadService {
    fun hentSøknad(ident: String, søknadId: SøknadId): StorSøknadDTO?

    fun hentBehandlingAvSøknad(søknadId: String, saksbehandler: Saksbehandler): BehandlingDTO?
}

data class BehandlingDTO(
    val personopplysninger: PersonopplysningerDTO,
    val søknad: SøknadDTO,
    val registrerteTiltak: List<TiltakDTO>,
    val vurderingsperiode: PeriodeDTO,
    val vurderinger: List<VilkårsVurderingsKategoriDTO>,
)

data class SøknadDTO(
    val søknadId: String,
    val søknadsdato: LocalDate,
    val arrangoernavn: String?,
    val tiltakskode: String?,
    val startdato: LocalDate,
    val sluttdato: LocalDate?,
    val antallDager: Int,
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
    val tittel: String,
    val lovreferanse: String,
    val utfall: UtfallDTO,
    val detaljer: String,
    val vilkårsvurderinger: List<VilkårsvurderingDTO>,
)

data class VilkårsvurderingDTO(
    val utfall: UtfallDTO,
    val vilkår: String,
    val periode: PeriodeDTO?,
    val kilde: String,
    val detaljer: String,
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

data class StorSøknadDTO(
    val søknadId: String,
)
