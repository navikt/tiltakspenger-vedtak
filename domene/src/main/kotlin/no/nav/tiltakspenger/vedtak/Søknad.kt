@file:Suppress("LongParameterList", "UnusedPrivateMember")

package no.nav.tiltakspenger.vedtak

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SøknadId
import java.time.LocalDate
import java.time.LocalDateTime

data class Søknad(
    val versjon: String = "1",
    val id: SøknadId = randomId(),
    val søknadId: String,
    val journalpostId: String,
    val dokumentInfoId: String,
    val personopplysninger: Personopplysninger,
    val kvp: PeriodeSpm,
    val intro: PeriodeSpm,
    val institusjon: PeriodeSpm,

    val etterlønn: Boolean,
    val alderspensjon: LocalDate?,
    val sykepenger: PeriodeSpm,
    val supplerendeStønadAlder: PeriodeSpm,
    val supplerendeStønadFlyktning: PeriodeSpm,

    val opprettet: LocalDateTime?,
    val barnetillegg: List<Barnetillegg>,
    val tidsstempelHosOss: LocalDateTime,
    val tiltak: Tiltak?,
    val trygdOgPensjon: List<TrygdOgPensjon>,
    val fritekst: String?,
    val vedlegg: List<Vedlegg>,
) : Tidsstempler {

    companion object {
        fun randomId() = SøknadId.random()
    }

    override fun tidsstempelKilde(): LocalDateTime = opprettet ?: tidsstempelHosOss()

    override fun tidsstempelHosOss(): LocalDateTime = tidsstempelHosOss

    data class Personopplysninger(
        val ident: String,
        val fornavn: String,
        val etternavn: String,
    )

    data class Kvp(
        val deltar: Boolean,
        val periode: Periode?,
    )

    sealed class PeriodeSpm {
        object IkkeMedISøknaden : PeriodeSpm()
        object IkkeRelevant : PeriodeSpm()
        object Nei : PeriodeSpm()
        data class Ja(
            val periode: Periode,
        ) : PeriodeSpm()
    }

    sealed class JaNeiSpm {
        object IkkeMedISøknaden : JaNeiSpm()
        object IkkeRelevant : JaNeiSpm()
        object Ja : JaNeiSpm()
        object Nei : JaNeiSpm()
    }

    sealed class FraOgMedDatoSpm {
        object IkkeMedISøknaden : JaNeiSpm()
        object IkkeRelevant : JaNeiSpm()
        object Ja : JaNeiSpm()
        object Nei : JaNeiSpm()
    }

//    data class Inddtro(
//        val erBesvart: Boolean,
//        val erRelevant: Boolean,
//        val besvart: Boolean,
//
//        val deltar: Boolean,
//        val periode: Periode?,
//    )
}

data class Vedlegg(
    val journalpostId: String,
    val dokumentInfoId: String,
    val filnavn: String?,
)

data class TrygdOgPensjon(
    val utbetaler: String,
    val prosent: Int? = null,
    val fom: LocalDate? = null,
    val tom: LocalDate? = null,
)

//data class Tiltak(
//    val arenaId: String,
//    val periode: Periode,
//    val opprinneligStartdato: LocalDate,
//    val opprinneligSluttdato: LocalDate?,
//    val arrangør: String,
//    val type: Tiltaksaktivitet.Tiltak,
//)

sealed class Tiltak {

    abstract val arrangoernavn: String?
    abstract val tiltakskode: Tiltaksaktivitet.Tiltak?
    abstract val startdato: LocalDate
    abstract val sluttdato: LocalDate?

    data class ArenaTiltak(
        val arenaId: String,
        override val arrangoernavn: String?, // Er null hvis arrangør er NAV selv.
        val harSluttdatoFraArena: Boolean,
        override val tiltakskode: Tiltaksaktivitet.Tiltak,
        val erIEndreStatus: Boolean,
        val opprinneligSluttdato: LocalDate? = null,
        val opprinneligStartdato: LocalDate,
        override val sluttdato: LocalDate? = null,
        override val startdato: LocalDate,
    ) : Tiltak()
}

sealed class Barnetillegg {
    abstract val alder: Int
    abstract val oppholdsland: String
    abstract val fornavn: String?
    abstract val mellomnavn: String?
    abstract val etternavn: String?
    abstract val søktBarnetillegg: Boolean

    data class MedIdent(
        override val alder: Int,
        override val oppholdsland: String,
        override val fornavn: String?,
        override val mellomnavn: String?,
        override val etternavn: String?,
        val ident: String,
        override val søktBarnetillegg: Boolean,
    ) : Barnetillegg()

    data class UtenIdent(
        override val alder: Int,
        override val oppholdsland: String,
        override val fornavn: String?,
        override val mellomnavn: String?,
        override val etternavn: String?,
        val fødselsdato: LocalDate,
        override val søktBarnetillegg: Boolean,
    ) : Barnetillegg()
}

enum class TypeInstitusjon(val type: String) {
    BARNEVERN("barneverninstitusjon"),
    OVERGANGSBOLIG("overgangsbolig"),
    ANNET("annet"),
}
