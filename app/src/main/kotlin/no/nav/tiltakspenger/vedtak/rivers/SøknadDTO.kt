@file:Suppress("LongParameterList")

package no.nav.tiltakspenger.vedtak.rivers

import java.time.LocalDate
import java.time.LocalDateTime

data class SøknadDTO(
    val versjon: String,
    val søknadId: String,
    val dokInfo: DokumentInfoDTO,
    val personopplysninger: PersonopplysningerDTO,
    val arenaTiltak: ArenaTiltakDTO?,
    val brukerTiltak: BrukerTiltakDTO?,
    val barnetilleggPdl: List<BarnetilleggDTO>,
    val barnetilleggManuelle: List<BarnetilleggDTO>,
    val vedlegg: List<DokumentInfoDTO>,
    val kvp: PeriodeSpmDTO,
    val intro: PeriodeSpmDTO,
    val institusjon: PeriodeSpmDTO,
    val etterlønn: JaNeiSpmDTO,
    val gjenlevendepensjon: FraOgMedDatoSpmDTO,
    val alderspensjon: FraOgMedDatoSpmDTO,
    val sykepenger: PeriodeSpmDTO,
    val supplerendeStønadAlder: PeriodeSpmDTO,
    val supplerendeStønadFlyktning: PeriodeSpmDTO,
    val jobbsjansen: PeriodeSpmDTO,
    val trygdOgPensjon: FraOgMedDatoSpmDTO,
    val opprettet: LocalDateTime,
)

data class DokumentInfoDTO(
    val journalpostId: String,
    val dokumentInfoId: String,
    val filnavn: String,
)

data class PersonopplysningerDTO(
    val ident: String,
    val fornavn: String,
    val etternavn: String,
)

data class ArenaTiltakDTO(
    val arenaId: String,
    val arrangoernavn: String?,
    val tiltakskode: String,
    val opprinneligSluttdato: LocalDate? = null,
    val opprinneligStartdato: LocalDate,
    val sluttdato: LocalDate? = null,
    val startdato: LocalDate,
)

data class BrukerTiltakDTO(
    val tiltakskode: String,
    val arrangoernavn: String?,
    val beskrivelse: String?,
    val fom: LocalDate,
    val tom: LocalDate,
    val adresse: String? = null,
    val postnummer: String? = null,
    val antallDager: Int,
)

data class BarnetilleggDTO(
    val fødselsdato: LocalDate?,
    val fornavn: String?,
    val mellomnavn: String?,
    val etternavn: String?,
    val oppholderSegIEØS: JaNeiSpmDTO,
)

data class JaNeiSpmDTO(
    val svar: SpmSvarDTO,
)

data class PeriodeSpmDTO(
    val svar: SpmSvarDTO,
    val fom: LocalDate?,
    val tom: LocalDate?,
)

data class FraOgMedDatoSpmDTO(
    val svar: SpmSvarDTO,
    val fom: LocalDate?,
)

enum class SpmSvarDTO {
    IkkeMedISøknaden,
    IkkeRelevant,
    IkkeBesvart,
    FeilaktigBesvart,
    Nei,
    Ja,
}
