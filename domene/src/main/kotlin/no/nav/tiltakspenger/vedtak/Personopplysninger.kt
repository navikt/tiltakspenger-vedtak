package no.nav.tiltakspenger.vedtak

import java.time.LocalDate
import java.time.LocalDateTime

class Personopplysninger(
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
        visitor.visitPersonopplysninger(this)
    }

    override fun tidsstempelKilde(): LocalDateTime = innhentet
    override fun tidsstempelHosOss(): LocalDateTime = innhentet
}
