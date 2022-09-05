package no.nav.tiltakspenger.vedtak.routes.person

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.tiltakspenger.domene.Søknad
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

data class PersonDTO(
    val personopplysninger: PersonopplysningerDTO,
    val behandlinger: List<BehandlingDTO>,
)

data class BehandlingDTO(
    val id: String,
    val søknad: Søknad,
    val tiltak: TiltakDTO,
    val periode: PeriodeDTO,
    val vurderinger: List<VilkårsVurderingsKategori>,
)

data class PeriodeDTO(
    val fra: LocalDate,
    val til: LocalDate,
)
data class TiltakDTO(
    val arrangør: String,
    val navn: String,
    val periode: PeriodeDTO,
    val prosent: Int,
    val dagerIUken: Int,
    val status: String,
)

data class VilkårsVurderingsKategori(
    val tittel: String,
    val utfall: UtfallDTO,
    val vilkårsvurderinger: List<VilkårsvurderingDTO>,
)

data class VilkårsvurderingDTO(
    val utfall: UtfallDTO,
    val periode: PeriodeDTO?,
    val vilkår: String,
    val kilde: String,
)

enum class UtfallDTO {
    Oppfylt,
    Uavklart,
    IkkeOppfylt,
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
    val ident: String,
    val bosted: String
)
