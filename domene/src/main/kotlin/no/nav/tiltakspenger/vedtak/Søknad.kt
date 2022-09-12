package no.nav.tiltakspenger.vedtak

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Suppress("LongParameterList", "UnusedPrivateMember")
class Søknad(
    private val id: UUID,
    private val fornavn: String?, //TODO Trenger vi denne? Henter den uansett fra PDL, som kan gi et annet svar
    private val etternavn: String?, //TODO Trenger vi denne? Henter den uansett fra PDL, som kan gi et annet svar
    private val ident: String,
    private val deltarKvp: Boolean,
    private val deltarIntroduksjonsprogrammet: Boolean?,
    private val oppholdInstitusjon: Boolean?,
    private val typeInstitusjon: String?, // TODO Høres ut som en enum
    private val tiltaksArrangoer: String?, // TODO Ikke mulig å få et org nr?
    private val tiltaksType: String?, // TODO Er en enum
    private val opprettet: LocalDateTime?,
    private val brukerRegistrertStartDato: LocalDate?,
    private val brukerRegistrertSluttDato: LocalDate?,
    private val systemRegistrertStartDato: LocalDate?,
    private val systemRegistrertSluttDato: LocalDate?,
    private val barnetillegg: List<Barnetillegg>,
    private val innhentet: LocalDateTime,
) : Tidsstempler {
    fun accept(visitor: SøkerVisitor) {
        visitor.visitSøknad(this)
    }

    override fun oppdatert(): LocalDateTime = opprettet ?: innhentet()

    override fun innhentet(): LocalDateTime = innhentet
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
