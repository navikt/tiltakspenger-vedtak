package no.nav.tiltakspenger.domene.personopplysninger

import java.time.LocalDate
import java.time.LocalDateTime

data class SøkerPersonopplysninger(
    val ident: String,
    val fødselsdato: LocalDate,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val fortrolig: Boolean,
    val strengtFortrolig: Boolean,
    val strengtFortroligUtland: Boolean,
    val skjermet: Boolean?,
    val kommune: String?,
    val bydel: String?,
    val tidsstempelHosOss: LocalDateTime, // innhentet gjelder PDL, ikke skjerming (som i teorien er litt etter)
) : Personopplysninger {
    override fun avklartSkjerming(): Boolean = skjermet ?: throw IllegalStateException("Skjerming er ikke satt")
    override fun strengtFortrolig(): Boolean = (strengtFortrolig || strengtFortroligUtland)
    override fun fortrolig(): Boolean = fortrolig
    override fun skjermet(): Boolean = skjermet ?: true

    override fun equals(other: Any?): Boolean {
        if (other !is SøkerPersonopplysninger) return false
        return this.ident == other.ident &&
            this.fødselsdato == other.fødselsdato &&
            this.fornavn == other.fornavn &&
            this.mellomnavn == other.mellomnavn &&
            this.etternavn == other.etternavn &&
            this.fortrolig == other.fortrolig &&
            this.strengtFortrolig == other.strengtFortrolig &&
            this.strengtFortroligUtland == other.strengtFortroligUtland &&
            this.skjermet == other.skjermet &&
            this.kommune == other.kommune &&
            this.bydel == other.bydel
    }
}
