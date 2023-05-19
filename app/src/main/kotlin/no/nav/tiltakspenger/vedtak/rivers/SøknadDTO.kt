@file:Suppress("LongParameterList")

package no.nav.tiltakspenger.vedtak.rivers

import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import java.time.LocalDate
import java.time.LocalDateTime

data class SøknadDTO(
    val søknadId: String,
    val versjon: String,
    val journalpostId: String,
    val dokumentInfoId: String,
    val personopplysninger: PersonopplysningerDTO,
    val kvalifiseringsprogram: KvalifiseringsprogramDTO,
    val introduksjonsprogram: IntroduksjonsprogramDTO,
    val oppholdInstitusjon: Boolean,
    val typeInstitusjon: String?,
    val opprettet: LocalDateTime,
    val barnetillegg: List<BarnetilleggDTO>,
    val arenaTiltak: ArenaTiltakDTO?,
    val brukerregistrertTiltak: BrukerregistrertTiltakDTO?,
    val trygdOgPensjon: List<TrygdOgPensjonDTO>? = emptyList(),
    val fritekst: String?,
    val vedlegg: List<VedleggDTO>? = emptyList(),
) {
    data class PersonopplysningerDTO(
        val ident: String,
        val fornavn: String,
        val etternavn: String,
    )

    data class PeriodeDTO(
        val fra: LocalDate,
        val til: LocalDate,
    )

    data class KvalifiseringsprogramDTO(
        val deltar: Boolean,
        val periode: PeriodeDTO?,
    )

    data class IntroduksjonsprogramDTO(
        val deltar: Boolean,
        val periode: PeriodeDTO?,
    )
}

class BrukerregistrertTiltakDTO(
    val tiltakskode: String,
    val arrangoernavn: String?,
    val beskrivelse: String?,
    val fom: LocalDate,
    val tom: LocalDate,
    val adresse: String? = null,
    val postnummer: String? = null,
    val antallDager: Int,
) {
    companion object {
        internal fun mapBrukerregistrertTiltak(dto: BrukerregistrertTiltakDTO?): Tiltak.BrukerregistrertTiltak? =
            if (dto == null) {
                null
            } else {
                Tiltak.BrukerregistrertTiltak(
                    tiltakskode = Tiltaksaktivitet.mapTiltaksType(dto.tiltakskode), // TODO:test
                    arrangoernavn = dto.arrangoernavn,
                    beskrivelse = dto.beskrivelse,
                    startdato = dto.fom,
                    sluttdato = dto.tom,
                    adresse = dto.adresse,
                    postnummer = dto.postnummer,
                    antallDager = dto.antallDager,
                )
            }
    }
}

class ArenaTiltakDTO(
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

class TrygdOgPensjonDTO(
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

class BarnetilleggDTO(
    val alder: Int,
    val oppholdsland: String,
    val ident: String? = null,
    val fødselsdato: LocalDate? = null,
    val fornavn: String? = null,
    val mellomnavn: String? = null,
    val etternavn: String? = null,
    val søktBarnetillegg: Boolean? = null, // Er midlertidig at det er null, endres når alt er i sync
)

@Suppress("ktlint:enum-entry-name-case")
enum class TypeInstitusjonDTO(val type: String) {
    barneverninstitusjon("barneverninstitusjon"),
    overgangsbolig("overgangsbolig"),
    annet("annet"),
}
