@file:Suppress("LongParameterList", "UnusedPrivateMember")

package no.nav.tiltakspenger.saksbehandling.behandling

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SøknadId
import java.time.LocalDate
import java.time.LocalDateTime

data class Søknad(
    val versjon: String = "1",
    val id: SøknadId = randomId(),
    val søknadId: String, // TODO: Bør denne ha et annet navn? Hvor kommer den fra?
    val journalpostId: String, // TODO: Skille ut i Vedlegg-klasse, som bør få annet navn. Trenger å få med filnavn fra mottak!
    val dokumentInfoId: String,
    val filnavn: String,
    val personopplysninger: Personopplysninger,
    val tiltak: SøknadsTiltak,
    val barnetillegg: List<Barnetillegg>,
    val opprettet: LocalDateTime,
    val tidsstempelHosOss: LocalDateTime,
    val vedlegg: List<Vedlegg>,
    val kvp: PeriodeSpm,
    val intro: PeriodeSpm,
    val institusjon: PeriodeSpm,
    val etterlønn: JaNeiSpm,
    val gjenlevendepensjon: PeriodeSpm,
    val alderspensjon: FraOgMedDatoSpm,
    val sykepenger: PeriodeSpm,
    val supplerendeStønadAlder: PeriodeSpm,
    val supplerendeStønadFlyktning: PeriodeSpm,
    val jobbsjansen: PeriodeSpm,
    val trygdOgPensjon: PeriodeSpm,
) {

    companion object {
        fun randomId() = SøknadId.random()
    }

    fun vurderingsperiode(): Periode {
        return Periode(tiltak.deltakelseFom, tiltak.deltakelseTom)
    }

    data class Personopplysninger(
        val ident: String,
        val fornavn: String,
        val etternavn: String,
    )

    sealed class PeriodeSpm {
        data object Nei : PeriodeSpm()
        data class Ja(
            val periode: Periode,
        ) : PeriodeSpm()
    }

    sealed class JaNeiSpm {
        data object Ja : JaNeiSpm()
        data object Nei : JaNeiSpm()
    }

    sealed class FraOgMedDatoSpm {
        data object Nei : FraOgMedDatoSpm()
        data class Ja(
            val fra: LocalDate,
        ) : FraOgMedDatoSpm()
    }
}

data class Vedlegg(
    val journalpostId: String,
    val dokumentInfoId: String,
    val filnavn: String?,
)

data class SøknadsTiltak(
    val id: String,
    val deltakelseFom: LocalDate,
    val deltakelseTom: LocalDate,
    val arrangør: String,
    val typeKode: String,
    val typeNavn: String,
)

sealed class Barnetillegg {
    abstract val oppholderSegIEØS: Søknad.JaNeiSpm
    abstract val fornavn: String?
    abstract val mellomnavn: String?
    abstract val etternavn: String?
    abstract val fødselsdato: LocalDate

    data class FraPdl(
        override val oppholderSegIEØS: Søknad.JaNeiSpm,
        override val fornavn: String?,
        override val mellomnavn: String?,
        override val etternavn: String?,
        override val fødselsdato: LocalDate,
    ) : Barnetillegg()

    data class Manuell(
        override val oppholderSegIEØS: Søknad.JaNeiSpm,
        override val fornavn: String,
        override val mellomnavn: String?,
        override val etternavn: String,
        override val fødselsdato: LocalDate,
    ) : Barnetillegg()
}
