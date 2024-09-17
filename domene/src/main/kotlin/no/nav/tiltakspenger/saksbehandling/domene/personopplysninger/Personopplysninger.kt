package no.nav.tiltakspenger.saksbehandling.domene.personopplysninger

import no.nav.tiltakspenger.libs.common.Fnr

sealed interface Personopplysninger {
    fun avklartSkjerming(): Boolean

    fun strengtFortrolig(): Boolean

    fun fortrolig(): Boolean

    fun skjermet(): Boolean
}

sealed interface PersonopplysningerMedIdent : Personopplysninger {
    fun fnr(): Fnr
}

fun List<Personopplysninger>.søkere(): List<PersonopplysningerSøker> = this.filterIsInstance<PersonopplysningerSøker>()

fun List<Personopplysninger>.søkerOrNull(): PersonopplysningerSøker? =
    this.filterIsInstance<PersonopplysningerSøker>().firstOrNull()

fun List<Personopplysninger>.søker(): PersonopplysningerSøker = this.filterIsInstance<PersonopplysningerSøker>().first()

fun List<Personopplysninger>.barnMedIdent(): List<PersonopplysningerBarnMedIdent> =
    this.filterIsInstance<PersonopplysningerBarnMedIdent>()

fun List<Personopplysninger>.barnUtenIdent(): List<PersonopplysningerBarnUtenIdent> =
    this.filterIsInstance<PersonopplysningerBarnUtenIdent>()
