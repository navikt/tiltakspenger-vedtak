package no.nav.tiltakspenger.saksbehandling.domene.personopplysninger

import mu.KotlinLogging
import no.nav.tiltakspenger.libs.common.Fnr

private val SECURELOG = KotlinLogging.logger("tjenestekall")

sealed interface Personopplysninger {
    fun avklartSkjerming(): Boolean

    fun strengtFortrolig(): Boolean

    fun fortrolig(): Boolean

    fun skjermet(): Boolean
}

sealed interface PersonopplysningerMedIdent : Personopplysninger {
    fun fnr(): Fnr
}

// TODO: Det er en del funksjoner her som fremdeles er i bruk, da jeg ikke har innført SakPersonopplysninger overalt ennå (mangler Innsending)
fun List<Personopplysninger>.søkere(): List<PersonopplysningerSøker> = this.filterIsInstance<PersonopplysningerSøker>()

fun List<Personopplysninger>.søkerOrNull(): PersonopplysningerSøker? = this.filterIsInstance<PersonopplysningerSøker>().firstOrNull()

fun List<Personopplysninger>.søker(): PersonopplysningerSøker = this.filterIsInstance<PersonopplysningerSøker>().first()

fun List<Personopplysninger>.barnMedIdent(): List<PersonopplysningerBarnMedIdent> = this.filterIsInstance<PersonopplysningerBarnMedIdent>()

fun List<Personopplysninger>.barnUtenIdent(): List<PersonopplysningerBarnUtenIdent> =
    this.filterIsInstance<PersonopplysningerBarnUtenIdent>()
