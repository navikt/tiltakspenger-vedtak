@file:Suppress("LongParameterList", "UnusedPrivateMember")

package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.periodisering.Periode
import java.time.LocalDate
import java.time.LocalDateTime

data class Søknad(
    val versjon: String = "1",
    val id: SøknadId,
    // TODO ?-mvp: Skille ut i Vedlegg-klasse, som bør få annet navn. Trenger å få med filnavn fra mottak!
    val journalpostId: String,
    val personopplysninger: Personopplysninger,
    val tiltak: Søknadstiltak,
    val barnetillegg: List<Barnetillegg>,
    val opprettet: LocalDateTime,
    val tidsstempelHosOss: LocalDateTime,
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
    val vedlegg: Int,
) {
    val fnr: Fnr = personopplysninger.fnr

    companion object {
        fun randomId() = SøknadId.random()
    }

    fun vurderingsperiode(): Periode = Periode(tiltak.deltakelseFom, tiltak.deltakelseTom)

    fun harLivsoppholdYtelser(): Boolean =
        sykepenger.erJa() ||
            etterlønn.erJa() ||
            trygdOgPensjon.erJa() ||
            gjenlevendepensjon.erJa() ||
            supplerendeStønadAlder.erJa() ||
            supplerendeStønadFlyktning.erJa() ||
            alderspensjon.erJa() ||
            jobbsjansen.erJa() ||
            trygdOgPensjon.erJa()

    data class Personopplysninger(
        val fnr: Fnr,
        val fornavn: String,
        val etternavn: String,
    )

    sealed interface PeriodeSpm {
        data object Nei : PeriodeSpm

        data class Ja(
            val periode: Periode,
        ) : PeriodeSpm

        /** ignorerer perioden */
        fun erJa(): Boolean =
            when (this) {
                is Ja -> true
                is Nei -> false
            }
    }

    sealed interface JaNeiSpm {
        data object Ja : JaNeiSpm

        data object Nei : JaNeiSpm

        /** ignorerer perioden */
        fun erJa(): Boolean =
            when (this) {
                is Ja -> true
                Nei -> false
            }
    }

    sealed interface FraOgMedDatoSpm {
        data object Nei : FraOgMedDatoSpm

        data class Ja(
            val fra: LocalDate,
        ) : FraOgMedDatoSpm

        fun erJa(): Boolean =
            when (this) {
                is Ja -> true
                is Nei -> false
            }
    }
}

data class Søknadstiltak(
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

    abstract fun under16ForDato(dato: LocalDate): Boolean

    data class FraPdl(
        override val oppholderSegIEØS: Søknad.JaNeiSpm,
        override val fornavn: String?,
        override val mellomnavn: String?,
        override val etternavn: String?,
        override val fødselsdato: LocalDate,
    ) : Barnetillegg() {
        override fun under16ForDato(dato: LocalDate): Boolean = fødselsdato.plusYears(16) > dato
    }

    data class Manuell(
        override val oppholderSegIEØS: Søknad.JaNeiSpm,
        override val fornavn: String,
        override val mellomnavn: String?,
        override val etternavn: String,
        override val fødselsdato: LocalDate,
    ) : Barnetillegg() {
        override fun under16ForDato(dato: LocalDate): Boolean = fødselsdato.plusYears(16) > dato
    }
}
