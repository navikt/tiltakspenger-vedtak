package no.nav.tiltakspenger.domene.behandling

import java.time.LocalDate
import java.time.LocalDateTime

sealed class Personopplysninger {
    abstract fun avklartSkjerming(): Boolean
    data class Søker(
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
    ) : Personopplysninger() {
        override fun avklartSkjerming(): Boolean = skjermet ?: throw IllegalStateException("Skjerming er ikke satt")

        override fun equals(other: Any?): Boolean {
            if (other !is Søker) return false
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

    data class BarnUtenIdent(
        val fødselsdato: LocalDate?,
        val fornavn: String?,
        val mellomnavn: String?,
        val etternavn: String?,
        val tidsstempelHosOss: LocalDateTime, // innhentet gjelder PDL, ikke skjerming (som i teorien er litt etter)
    ) : Personopplysninger() {
        override fun avklartSkjerming(): Boolean = false

        override fun equals(other: Any?): Boolean {
            if (other !is BarnUtenIdent) return false
            return this.fødselsdato == other.fødselsdato &&
                this.fornavn == other.fornavn &&
                this.etternavn == other.etternavn &&
                this.mellomnavn == other.mellomnavn
        }
    }

    data class BarnMedIdent(
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
    ) : Personopplysninger() {
        override fun avklartSkjerming(): Boolean = skjermet ?: throw IllegalStateException("Skjerming er ikke satt")

        override fun equals(other: Any?): Boolean {
            if (other !is BarnMedIdent) return false
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
}

fun List<Personopplysninger>.erLik(personopplysninger: List<Personopplysninger>): Boolean {
    if (this.size != personopplysninger.size) return false
    return this.all { person ->
        personopplysninger.any { it == person }
    }
}
