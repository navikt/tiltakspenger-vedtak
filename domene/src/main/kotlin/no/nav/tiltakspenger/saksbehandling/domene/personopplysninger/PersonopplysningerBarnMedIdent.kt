package no.nav.tiltakspenger.saksbehandling.domene.personopplysninger

import java.time.LocalDate
import java.time.LocalDateTime

data class PersonopplysningerBarnMedIdent(
    val ident: String,
    val fødselsdato: LocalDate,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val fortrolig: Boolean,
    val strengtFortrolig: Boolean,
    val strengtFortroligUtland: Boolean,
    val skjermet: Boolean?,
    val oppholdsland: String?,
    val tidsstempelHosOss: LocalDateTime, // innhentet gjelder PDL, ikke skjerming (som i teorien er litt etter)
) : PersonopplysningerMedIdent {
    override fun avklartSkjerming(): Boolean = skjermet ?: throw IllegalStateException("Skjerming er ikke satt")
    override fun strengtFortrolig(): Boolean = (strengtFortrolig || strengtFortroligUtland)
    override fun fortrolig(): Boolean = fortrolig
    override fun skjermet(): Boolean = skjermet ?: true
    override fun ident(): String = ident

    override fun equals(other: Any?): Boolean {
        if (other !is PersonopplysningerBarnMedIdent) return false
        return this.ident == other.ident &&
            this.fødselsdato == other.fødselsdato &&
            this.fornavn == other.fornavn &&
            this.etternavn == other.etternavn &&
            this.mellomnavn == other.mellomnavn &&
            this.fortrolig == other.fortrolig &&
            this.strengtFortrolig == other.strengtFortrolig &&
            this.strengtFortroligUtland == other.strengtFortroligUtland &&
            this.skjermet == other.skjermet &&
            this.oppholdsland == other.oppholdsland
    }
}
