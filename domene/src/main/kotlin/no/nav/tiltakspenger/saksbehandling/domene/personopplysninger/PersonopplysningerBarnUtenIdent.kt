package no.nav.tiltakspenger.saksbehandling.domene.personopplysninger

import java.time.LocalDate
import java.time.LocalDateTime

data class PersonopplysningerBarnUtenIdent(
    val fødselsdato: LocalDate?,
    val fornavn: String?,
    val mellomnavn: String?,
    val etternavn: String?,
    val tidsstempelHosOss: LocalDateTime, // innhentet gjelder PDL, ikke skjerming (som i teorien er litt etter)
) : Personopplysninger {
    override fun avklartSkjerming(): Boolean = false
    override fun strengtFortrolig(): Boolean = false
    override fun fortrolig(): Boolean = false
    override fun skjermet(): Boolean = false

    override fun equals(other: Any?): Boolean {
        if (other !is PersonopplysningerBarnUtenIdent) return false
        return this.fødselsdato == other.fødselsdato &&
            this.fornavn == other.fornavn &&
            this.etternavn == other.etternavn &&
            this.mellomnavn == other.mellomnavn
    }
}
