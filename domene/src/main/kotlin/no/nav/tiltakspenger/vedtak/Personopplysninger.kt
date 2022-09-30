package no.nav.tiltakspenger.vedtak

import java.time.LocalDate
import java.time.LocalDateTime

@Suppress("LongParameterList")
data class Personopplysninger(
    val ident: String,
    val fødselsdato: LocalDate,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val fortrolig: Boolean,
    val strengtFortrolig: Boolean,
    val skjermet: Boolean?,
    val kommune: String?,
    val bydel: String?,
    val land: String?,
    val tidsstempelHosOss: LocalDateTime // innhentet gjelder PDL, ikke skjerming (som i teorien er litt etter)){}){}
) : Tidsstempler {
    fun accept(visitor: SøkerVisitor) {
        visitor.visitPersonopplysninger(this)
    }

    override fun tidsstempelKilde(): LocalDateTime = tidsstempelHosOss
    override fun tidsstempelHosOss(): LocalDateTime = tidsstempelHosOss
}
