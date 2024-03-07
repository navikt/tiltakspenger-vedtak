package no.nav.tiltakspenger.domene.personopplysninger

import mu.KotlinLogging
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.Saksbehandler

private val SECURELOG = KotlinLogging.logger("tjenestekall")

sealed interface Personopplysninger {
    fun avklartSkjerming(): Boolean
    fun strengtFortrolig(): Boolean
    fun fortrolig(): Boolean
    fun skjermet(): Boolean
}

fun List<Personopplysninger>.søkere(): List<PersonopplysningerSøker> = this.filterIsInstance<PersonopplysningerSøker>()
fun List<Personopplysninger>.søkerOrNull(): PersonopplysningerSøker? =
    this.filterIsInstance<PersonopplysningerSøker>().firstOrNull()

fun List<Personopplysninger>.søkerMedIdent(ident: String): PersonopplysningerSøker? =
    this.filterIsInstance<PersonopplysningerSøker>().firstOrNull { it.ident == ident }

fun List<Personopplysninger>.søker(): PersonopplysningerSøker = this.filterIsInstance<PersonopplysningerSøker>().first()
fun List<Personopplysninger>.barnMedIdent(): List<PersonopplysningerBarnMedIdent> =
    this.filterIsInstance<PersonopplysningerBarnMedIdent>()

fun List<Personopplysninger>.barnUtenIdent(): List<PersonopplysningerBarnUtenIdent> =
    this.filterIsInstance<PersonopplysningerBarnUtenIdent>()

fun List<Personopplysninger>.erLik(personopplysninger: List<Personopplysninger>): Boolean {
    if (this.size != personopplysninger.size) return false
    return this.all { person ->
        personopplysninger.any { it == person }
    }
}

fun List<Personopplysninger>.harTilgang(saksbehandler: Saksbehandler): Boolean {
    if (this.any { it.strengtFortrolig() }) return Rolle.STRENGT_FORTROLIG_ADRESSE in saksbehandler.roller
    if (this.any { it.fortrolig() }) return Rolle.FORTROLIG_ADRESSE in saksbehandler.roller
    if (this.any { it.skjermet() }) return Rolle.SKJERMING in saksbehandler.roller
    return true
}
