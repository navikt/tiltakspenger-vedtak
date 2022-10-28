package no.nav.tiltakspenger.vedtak.routes.person

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.time.LocalDate

val objectMapper: ObjectMapper = JsonMapper.builder()
    .addModule(JavaTimeModule())
    .addModule(KotlinModule.Builder().build())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .disable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)
    .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
    .enable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
    .build()

fun serialize(value: Any): String = objectMapper.writeValueAsString(value)

data class BehandlingDTO(
    val personopplysninger: PersonopplysningerDTO,
    val søknad: SøknadDTO,
    val registrerteTiltak: List<TiltakDTO>,
    val vurderingsperiode: PeriodeDTO,
    val vurderinger: List<VilkårsVurderingsKategori>,
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

data class VilkårsVurderingsKategori(
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
    val fornavn: String,
    val etternavn: String,
    val ident: String,
    val barn: List<BarnDTO>,
)

data class BarnDTO(
    val fornavn: String,
    val etternavn: String,
    val ident: String?,
    val bosted: String?
)
