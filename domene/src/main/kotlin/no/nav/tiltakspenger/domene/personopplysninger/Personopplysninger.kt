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

// TODO: Hadde det vært en idé å wrappet denne lista i en klasse AllePersonopplysninger (el.)
// og hatt metoder på den for søker(), barnMedIdent() osv?
// En List<Personopplysninger> sier ingenting om hvilke forretningsregler som gjelder,
// om det kan være en eller flere søkere, om det alltid må være en søker, om noe som helst..

// TODO: Hvorfor søke på ident hvis det bare kan være én søker?
fun List<Personopplysninger>.søker(ident: String): SøkerPersonopplysninger? =
    this.filterIsInstance<SøkerPersonopplysninger>()
        .firstOrNull { søker -> søker.ident == ident }

fun List<Personopplysninger>.søker(): SøkerPersonopplysninger =
    this.filterIsInstance<SøkerPersonopplysninger>()
        .first()

// TODO: Ett steder hentes søkere (flertall) fra lista, skal det være en mulig tilstand?
fun List<Personopplysninger>.søkere(): List<SøkerPersonopplysninger> =
    this.filterIsInstance<SøkerPersonopplysninger>()

fun List<Personopplysninger>?.søkerOrNull(): SøkerPersonopplysninger? =
    this?.filterIsInstance<SøkerPersonopplysninger>()
        ?.firstOrNull()

fun List<Personopplysninger>.barnMedIdent(ident: String): BarnMedIdentPersonopplysninger? =
    this.barnMedIdent()
        .firstOrNull { barn -> barn.ident == ident }

fun List<Personopplysninger>.barnMedIdent(): List<BarnMedIdentPersonopplysninger> =
    this.filterIsInstance<BarnMedIdentPersonopplysninger>()

fun List<Personopplysninger>.barnUtenIdent(): List<BarnUtenIdentPersonopplysninger> =
    this.filterIsInstance<BarnUtenIdentPersonopplysninger>()
