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
    val bosted: String?,
    val skjermet: Boolean?, // TODO: bør vi gjøre om denne til en val og heller kopiere objektet når vi endrer verdien?
    val innhentet: LocalDateTime // innhentet gjelder PDL, ikke skjerming (som i teorien er litt etter)
) : Tidsstempler {
    fun accept(visitor: SøkerVisitor) {
        visitor.visitPersonopplysninger(this)
    }

    override fun tidsstempelKilde(): LocalDateTime = innhentet
    override fun tidsstempelHosOss(): LocalDateTime = innhentet
}
