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
    val etterlønn: JaNeiSpm,
    val gjenlevendepensjon: FraOgMedDatoSpm,
    val alderspensjon: FraOgMedDatoSpm,
    val sykepenger: PeriodeSpm,
    val supplerendeStønadAlder: PeriodeSpm,
    val supplerendeStønadFlyktning: PeriodeSpm,
    val jobbsjansen: PeriodeSpm,
    val trygdOgPensjon: FraOgMedDatoSpm,
    val tiltak: Tiltak?,
    val barnetillegg: List<Barnetillegg>,
    val innsendt: LocalDateTime,
    val tidsstempelHosOss: LocalDateTime,
    val vedlegg: List<Vedlegg>,
) : Tidsstempler {

    companion object {
        fun randomId() = SøknadId.random()
    }

    override fun tidsstempelKilde(): LocalDateTime = innsendt ?: tidsstempelHosOss()

    override fun tidsstempelHosOss(): LocalDateTime = tidsstempelHosOss

    data class Personopplysninger(
        val ident: String,
        val fornavn: String,
        val etternavn: String,
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
        object IkkeMedISøknaden : FraOgMedDatoSpm()
        object IkkeRelevant : FraOgMedDatoSpm()
        data class Ja(
            val fra: LocalDate,
        ) : FraOgMedDatoSpm()
        object Nei : FraOgMedDatoSpm()
    }
}

data class Vedlegg(
    val journalpostId: String,
    val dokumentInfoId: String,
    val filnavn: String?,
)

sealed class Tiltak {

    abstract val arrangoernavn: String?
    abstract val tiltakskode: Tiltaksaktivitet.Tiltak?
    abstract val startdato: LocalDate
    abstract val sluttdato: LocalDate?

    data class ArenaTiltak(
        val arenaId: String,
        override val arrangoernavn: String?, // Er null hvis arrangør er NAV selv.
        override val tiltakskode: Tiltaksaktivitet.Tiltak,
        val opprinneligSluttdato: LocalDate? = null,
        val opprinneligStartdato: LocalDate,
        override val sluttdato: LocalDate? = null,
        override val startdato: LocalDate,
    ) : Tiltak()

    data class BrukerregistrertTiltak(
        override val tiltakskode: Tiltaksaktivitet.Tiltak?, // Er null hvis bruker velger "Annet" i søknaden
        override val arrangoernavn: String?, // Er null om f.eks. kode 6
        val beskrivelse: String?,
        override val startdato: LocalDate,
        override val sluttdato: LocalDate,
        val adresse: String? = null,
        val postnummer: String? = null,
        val antallDager: Int,
    ) : Tiltak()
}
sealed class Barnetillegg {
    abstract val oppholderSegIEØS: Boolean
    abstract val fornavn: String?
    abstract val mellomnavn: String?
    abstract val etternavn: String?
    abstract val fødselsdato: LocalDate

    data class FraPdl(
        override val oppholderSegIEØS: Boolean,
        override val fornavn: String?,
        override val mellomnavn: String?,
        override val etternavn: String?,
        override val fødselsdato: LocalDate,
    ) : Barnetillegg()

    data class Manuell(
        override val oppholderSegIEØS: Boolean,
        override val fornavn: String?,
        override val mellomnavn: String?,
        override val etternavn: String?,
        override val fødselsdato: LocalDate,
    ) : Barnetillegg()
}

//enum class TypeInstitusjon(val type: String) {
//    BARNEVERN("barneverninstitusjon"),
//    OVERGANGSBOLIG("overgangsbolig"),
//    ANNET("annet"),
//}
