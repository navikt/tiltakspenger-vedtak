@file:Suppress("LongParameterList", "UnusedPrivateMember")

package no.nav.tiltakspenger.vedtak

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
    val personopplysninger: Personopplysninger,
    val tiltak: Tiltak?,
    val barnetillegg: List<Barnetillegg>,
    val opprettet: LocalDateTime,
    val tidsstempelHosOss: LocalDateTime,
    val vedlegg: List<Vedlegg>,
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
) : Tidsstempler {

    companion object {
        fun randomId() = SøknadId.random()
    }

    override fun tidsstempelKilde(): LocalDateTime = opprettet

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

        object IkkeBesvart : PeriodeSpm()
        data class FeilaktigBesvart(
            val svartJa: Boolean?,
            val fom: LocalDate?,
            val tom: LocalDate?,
        ) : PeriodeSpm()
    }

    sealed class JaNeiSpm {
        object IkkeMedISøknaden : JaNeiSpm()
        object IkkeRelevant : JaNeiSpm()
        object Ja : JaNeiSpm()
        object Nei : JaNeiSpm()
        object IkkeBesvart : JaNeiSpm()
    }

    sealed class FraOgMedDatoSpm {
        object IkkeMedISøknaden : FraOgMedDatoSpm()
        object IkkeRelevant : FraOgMedDatoSpm()
        data class Ja(
            val fra: LocalDate,
        ) : FraOgMedDatoSpm()

        object Nei : FraOgMedDatoSpm()
        object IkkeBesvart : FraOgMedDatoSpm()
        data class FeilaktigBesvart(
            val svartJa: Boolean?,
            val fom: LocalDate?,
        ) : FraOgMedDatoSpm()
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
    abstract val oppholderSegIEØS: Søknad.JaNeiSpm
    abstract val fornavn: String
    abstract val mellomnavn: String?
    abstract val etternavn: String
    abstract val fødselsdato: LocalDate

    data class FraPdl(
        override val oppholderSegIEØS: Søknad.JaNeiSpm,
        override val fornavn: String,
        override val mellomnavn: String?,
        override val etternavn: String,
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

// enum class TypeInstitusjon(val type: String) {
//    BARNEVERN("barneverninstitusjon"),
//    OVERGANGSBOLIG("overgangsbolig"),
//    ANNET("annet"),
// }
