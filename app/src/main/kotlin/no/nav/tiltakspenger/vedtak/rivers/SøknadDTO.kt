@file:Suppress("LongParameterList")

package no.nav.tiltakspenger.vedtak.rivers

import java.time.LocalDate
import java.time.LocalDateTime

data class SøknadDTO(
    val versjon: String,
    val søknadId: String,
    val journalpostId: String,
    val dokumentInfoId: String,
    val filnavn: String,
    val fornavn: String?,
    val etternavn: String?,
    val ident: String,
    val deltarKvp: Boolean,
    val deltarIntroduksjonsprogrammet: Boolean?,
    val introduksjonsprogrammetDetaljer: IntroduksjonsprogrammetDetaljerDTO?,
    val oppholdInstitusjon: Boolean,
    val typeInstitusjon: String?,
    val opprettet: LocalDateTime,
    val barnetillegg: List<BarnetilleggDTO>,
    val arenaTiltak: ArenaTiltakDTO?,
    val brukerregistrertTiltak: BrukerregistrertTiltakDTO?,
    val trygdOgPensjon: List<TrygdOgPensjonDTO>? = emptyList(),
    val fritekst: String?,
    val vedlegg: List<VedleggDTO>? = emptyList(),
)

data class BrukerregistrertTiltakDTO(
    val tiltakskode: String,
    val arrangoernavn: String?,
    val beskrivelse: String?,
    val fom: LocalDate,
    val tom: LocalDate,
    val adresse: String? = null,
    val postnummer: String? = null,
    val antallDager: Int,
)

data class ArenaTiltakDTO(
    val arenaId: String,
    val arrangoer: String?,
    val harSluttdatoFraArena: Boolean,
    val tiltakskode: String,
    val erIEndreStatus: Boolean,
    val opprinneligSluttdato: LocalDate? = null,
    val opprinneligStartdato: LocalDate,
    val sluttdato: LocalDate? = null,
    val startdato: LocalDate,
)

data class IntroduksjonsprogrammetDetaljerDTO(
    val fom: LocalDate,
    val tom: LocalDate? = null,
)

data class TrygdOgPensjonDTO(
    val utbetaler: String,
    val prosent: Int? = null,
    val fom: LocalDate? = null,
    val tom: LocalDate? = null,
)

data class VedleggDTO(
    val journalpostId: String,
    val dokumentInfoId: String,
    val filnavn: String?,
)

data class BarnetilleggDTO(
    val alder: Int,
    val oppholdsland: String,
    val ident: String? = null,
    val fødselsdato: LocalDate? = null,
    val fornavn: String? = null,
    val mellomnavn: String? = null,
    val etternavn: String? = null,
    val søktBarnetillegg: Boolean? = null, // Er midlertidig at det er null, endres når alt er i sync
)

enum class TypeInstitusjonDTO(val type: String) {
    BARNEVERNINSTITUSJON("barneverninstitusjon"),
    OVERGANGSBOLIG("overgangsbolig"),
    ANNET("annet"),
}
