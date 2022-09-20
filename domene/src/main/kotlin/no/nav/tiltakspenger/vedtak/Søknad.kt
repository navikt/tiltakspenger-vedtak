@file:Suppress("LongParameterList", "UnusedPrivateMember")

package no.nav.tiltakspenger.vedtak

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class Søknad(
    val id: UUID = UUID.randomUUID(),
    val søknadId: String,
    val journalpostId: String,
    val dokumentInfoId: String,
    val fornavn: String?,
    val etternavn: String?,
    val ident: String,
    val deltarKvp: Boolean,
    val deltarIntroduksjonsprogrammet: Boolean?,
    val oppholdInstitusjon: Boolean?,
    val typeInstitusjon: String?,
    val opprettet: LocalDateTime?,
    val barnetillegg: List<Barnetillegg>,
    val tidsstempelHosOss: LocalDateTime,
    val tiltak: Tiltak,
    val trygdOgPensjon: List<TrygdOgPensjon>,
    val fritekst: String?,
) : Tidsstempler {
    fun accept(visitor: SøkerVisitor) {
        visitor.visitSøknad(this)
    }

    override fun tidsstempelKilde(): LocalDateTime = opprettet ?: tidsstempelHosOss()

    override fun tidsstempelHosOss(): LocalDateTime = tidsstempelHosOss
}

class TrygdOgPensjon(
    val utbetaler: String,
    val prosent: Int? = null,
    val fom: LocalDate,
    val tom: LocalDate? = null
)

sealed class Tiltak {

    abstract val arrangoernavn: String?
    abstract val tiltakskode: Tiltaksaktivitet.Tiltak?
    abstract val startdato: LocalDate
    abstract val sluttdato: LocalDate

    data class ArenaTiltak(
        val arenaId: String,
        override val arrangoernavn: String?, // Er null hvis arrangør er NAV selv.
        val harSluttdatoFraArena: Boolean,
        override val tiltakskode: Tiltaksaktivitet.Tiltak,
        val erIEndreStatus: Boolean,
        val opprinneligSluttdato: LocalDate? = null,
        val opprinneligStartdato: LocalDate,
        override val sluttdato: LocalDate,
        override val startdato: LocalDate
    ) : Tiltak()

    data class BrukerregistrertTiltak(
        override val tiltakskode: Tiltaksaktivitet.Tiltak?, // Er null hvis bruker velger "Annet" i søknaden
        override val arrangoernavn: String?, // Er null
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
    abstract val land: String
    abstract val fornavn: String?
    abstract val etternavn: String?

    data class MedIdent(
        override val alder: Int,
        override val land: String,
        override val fornavn: String?,
        override val etternavn: String?,
        val ident: String,
    ) : Barnetillegg()

    data class UtenIdent(
        override val alder: Int,
        override val land: String,
        override val fornavn: String?,
        override val etternavn: String?,
        val fødselsdato: LocalDate,
    ) : Barnetillegg()
}
