package no.nav.tiltakspenger.vedtak

import java.time.LocalDate

class Person(
    val ident: String,
    val fødselsdato: LocalDate,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val fortrolig: Boolean,
    val strengtFortrolig: Boolean,
) {
    fun accept(visitor: SøkerVisitor) {
        visitor.visitPerson(this)
    }
}
