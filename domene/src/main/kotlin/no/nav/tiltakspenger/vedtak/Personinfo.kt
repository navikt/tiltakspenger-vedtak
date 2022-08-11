package no.nav.tiltakspenger.vedtak

import java.time.LocalDate
import java.time.LocalDateTime

class Personinfo(
    val ident: String,
    val fødselsdato: LocalDate,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val fortrolig: Boolean,
    val strengtFortrolig: Boolean,
    val innhentet: LocalDateTime,
) : Tidsstempler {
    fun accept(visitor: SøkerVisitor) {
        visitor.visitPersoninfo(this)
    }

    override fun oppdatert(): LocalDateTime = innhentet
    override fun innhentet(): LocalDateTime = innhentet
}
