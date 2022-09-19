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
    val fornavn: String?, //TODO Trenger vi denne? Henter den uansett fra PDL, som kan gi et annet svar
    val etternavn: String?, //TODO Trenger vi denne? Henter den uansett fra PDL, som kan gi et annet svar
    val ident: String,
    val deltarKvp: Boolean, // TODO Høres ut som en enum
    val deltarIntroduksjonsprogrammet: Boolean?, // TODO Ikke mulig å få et org nr?
    val oppholdInstitusjon: Boolean?, // TODO Er en enum
    val typeInstitusjon: String?,
    val opprettet: LocalDateTime?,
    val barnetillegg: List<Barnetillegg>,
    val tidsstempelHosOss: LocalDateTime,
    // TODO: Kan vi bruke sealed class som union type for å fange at man
    // *enten* har arenaTiltak *eller* brukerregistrertTiltak?
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
    data class ArenaTiltak(
        val arenaId: String? = null,
        val arrangoer: String? = null,
        val harSluttdatoFraArena: Boolean? = null,
        val tiltakskode: Tiltaksaktivitet.Tiltak? = null,
        val erIEndreStatus: Boolean? = null,
        val opprinneligSluttdato: LocalDate? = null,
        val opprinneligStartdato: LocalDate? = null,
        val sluttdato: LocalDate? = null,
        val startdato: LocalDate? = null
    ) : Tiltak()

    data class BrukerregistrertTiltak(
        val tiltakskode: Tiltaksaktivitet.Tiltak?,
        val arrangoernavn: String?,
        val beskrivelse: String?,
        val fom: LocalDate?,
        val tom: LocalDate?,
        val adresse: String? = null,
        val postnummer: String? = null,
        val antallDager: Int
    ) : Tiltak()
}

class Barnetillegg(
    val fornavn: String?, //TODO Trenger vi denne? Henter den uansett fra PDL, som kan gi et annet svar
    val etternavn: String?, //TODO Trenger vi denne? Henter den uansett fra PDL, som kan gi et annet svar
    val alder: Int, //TODO Trenger vi denne? Henter den uansett fra PDL, som kan gi et annet svar
    val ident: String,
    val land: String // TODO: Denne kan være sensitiv, hvis barnet er kode 6/7! Hva skal vi med den?
    // SVAR på over: Barnet må med virkning fra 1. juli 2020 være bosatt og oppholde seg i Norge, herunder Svalbard.
    // men TODO Trenger vi denne? Henter den uansett fra PDL, som kan gi et annet svar
)
