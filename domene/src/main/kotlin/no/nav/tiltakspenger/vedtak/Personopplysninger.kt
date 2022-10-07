package no.nav.tiltakspenger.vedtak

import java.time.LocalDate
import java.time.LocalDateTime

sealed class Personopplysninger() {
    abstract val fornavn: String

    @Suppress("LongParameterList")
    data class Søker(
        val ident: String,
        val fødselsdato: LocalDate?,
        override val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String,
        val fortrolig: Boolean,
        val strengtFortrolig: Boolean,
        val skjermet: Boolean?,
        val kommune: String?,
        val bydel: String?,
        val tidsstempelHosOss: LocalDateTime // innhentet gjelder PDL, ikke skjerming (som i teorien er litt etter)
    ) : Tidsstempler, Personopplysninger() {
        override fun tidsstempelKilde(): LocalDateTime = tidsstempelHosOss
        override fun tidsstempelHosOss(): LocalDateTime = tidsstempelHosOss
    }

    @Suppress("LongParameterList")
    data class BarnUtenIdent(
        val fødselsdato: LocalDate?,
        override val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String,
        val land: String?,
        val tidsstempelHosOss: LocalDateTime // innhentet gjelder PDL, ikke skjerming (som i teorien er litt etter)
    ) : Tidsstempler, Personopplysninger() {
        override fun tidsstempelKilde(): LocalDateTime = tidsstempelHosOss
        override fun tidsstempelHosOss(): LocalDateTime = tidsstempelHosOss
    }

    @Suppress("LongParameterList")
    data class BarnMedIdent(
        val ident: String,
        val fødselsdato: LocalDate,
        override val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String,
        val land: String?,
        val tidsstempelHosOss: LocalDateTime // innhentet gjelder PDL, ikke skjerming (som i teorien er litt etter)
    ) : Tidsstempler, Personopplysninger() {
        override fun tidsstempelKilde(): LocalDateTime = tidsstempelHosOss
        override fun tidsstempelHosOss(): LocalDateTime = tidsstempelHosOss
    }
}
