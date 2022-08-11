package no.nav.tiltakspenger.vedtak

import java.time.LocalDate
import java.time.LocalDateTime

@Suppress("LongParameterList", "UnusedPrivateMember")
class Søknad(
    private val id: String,
    private val fornavn: String?,
    private val etternavn: String?,
    private val ident: String,
    private val deltarKvp: Boolean,
    private val deltarIntroduksjonsprogrammet: Boolean?,
    private val oppholdInstitusjon: Boolean?,
    private val typeInstitusjon: String?,
    private val tiltaksArrangoer: String?,
    private val tiltaksType: String?,
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
    val fornavn: String?,
    val etternavn: String?,
    val alder: Int,
    val ident: String,
    val bosted: String
)