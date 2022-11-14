@file:Suppress("LongParameterList", "UnusedPrivateMember")

package no.nav.tiltakspenger.vedtak

import no.nav.tiltakspenger.felles.SøknadId
import java.time.LocalDate
import java.time.LocalDateTime

data class Søknad(
    val id: SøknadId = randomId(),
    val søknadId: String,
    val journalpostId: String,
    val dokumentInfoId: String,
    val fornavn: String?,
    val etternavn: String?,
    val ident: String,
    val deltarKvp: Boolean,
    val deltarIntroduksjonsprogrammet: Boolean,
    val introduksjonsprogrammetDetaljer: IntroduksjonsprogrammetDetaljer?,
    val oppholdInstitusjon: Boolean?,
    val typeInstitusjon: TypeInstitusjon?,
    val opprettet: LocalDateTime?,
    val barnetillegg: List<Barnetillegg>,
    val tidsstempelHosOss: LocalDateTime,
    val tiltak: Tiltak,
    val trygdOgPensjon: List<TrygdOgPensjon>,
    val fritekst: String?,
    val vedlegg: List<Vedlegg>
) : Tidsstempler {

    companion object {
        fun randomId() = SøknadId.random()
    }

    fun accept(visitor: SøkerVisitor) {
        visitor.visitSøknad(this)
    }

    override fun tidsstempelKilde(): LocalDateTime = opprettet ?: tidsstempelHosOss()

    override fun tidsstempelHosOss(): LocalDateTime = tidsstempelHosOss
}

data class Vedlegg(
    val journalpostId: String,
    val dokumentInfoId: String,
    val filnavn: String?,
)

data class IntroduksjonsprogrammetDetaljer(
    val fom: LocalDate,
    val tom: LocalDate?
)

data class TrygdOgPensjon(
    val utbetaler: String,
    val prosent: Int? = null,
    val fom: LocalDate? = null,
    val tom: LocalDate? = null
)

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
        override val startdato: LocalDate
    ) : Tiltak()

    data class BrukerregistrertTiltak(
        override val tiltakskode: Tiltaksaktivitet.Tiltak?, // Er null hvis bruker velger "Annet" i søknaden
        override val arrangoernavn: String?, // Er null om f.eks. kode 6
        val beskrivelse: String?,
        override val startdato: LocalDate,
        override val sluttdato: LocalDate,
        val adresse: String? = null,
        val postnummer: String? = null,
        val antallDager: Int
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
