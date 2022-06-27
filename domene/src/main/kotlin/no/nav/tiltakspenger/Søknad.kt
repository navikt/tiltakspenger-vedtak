package no.nav.tiltakspenger

import java.time.LocalDate
import java.time.LocalDateTime
import no.nav.tiltakspenger.vedtak.SøkerVisitor

class Søknad(
    val id: String,
    val fornavn: String?,
    val etternavn: String?,
    val ident: String,
    val deltarKvp: Boolean,
    val deltarIntroduksjonsprogrammet: Boolean?,
    val oppholdInstitusjon: Boolean?,
    val typeInstitusjon: String?,
    val tiltaksArrangoer: String?,
    val tiltaksType: String?,
    val opprettet: LocalDateTime?,
    val brukerRegistrertStartDato: LocalDate?,
    val brukerRegistrertSluttDato: LocalDate?,
    val systemRegistrertStartDato: LocalDate?,
    val systemRegistrertSluttDato: LocalDate?,
    val barnetillegg: List<Barnetillegg>
) {
    fun accept(visitor: SøkerVisitor) {
        visitor.visitSøknad(this)
    }
}

class Barnetillegg(
    val fornavn: String?,
    val etternavn: String?,
    val alder: Int,
    val ident: String,
    val bosted: String
)