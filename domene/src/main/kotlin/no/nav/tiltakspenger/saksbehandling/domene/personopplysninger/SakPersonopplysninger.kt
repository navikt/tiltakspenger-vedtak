package no.nav.tiltakspenger.saksbehandling.domene.personopplysninger

import no.nav.tiltakspenger.felles.Bruker
import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.felles.exceptions.TilgangException

data class SakPersonopplysninger(
    private val liste: List<Personopplysninger> = emptyList(),
) {

    fun søkerOrNull(): PersonopplysningerSøker? =
        liste.filterIsInstance<PersonopplysningerSøker>().firstOrNull()

    fun søkerMedIdent(ident: String): PersonopplysningerSøker? =
        liste.filterIsInstance<PersonopplysningerSøker>().firstOrNull { it.ident == ident }

    fun søker(): PersonopplysningerSøker =
        liste.filterIsInstance<PersonopplysningerSøker>().first()

    fun barnMedIdent(): List<PersonopplysningerBarnMedIdent> =
        liste.filterIsInstance<PersonopplysningerBarnMedIdent>()

    fun barnMedIdent(ident: String): PersonopplysningerBarnMedIdent? =
        liste.filterIsInstance<PersonopplysningerBarnMedIdent>().firstOrNull { it.ident == ident }

    fun barnUtenIdent(): List<PersonopplysningerBarnUtenIdent> =
        liste.filterIsInstance<PersonopplysningerBarnUtenIdent>()

    fun personerMedIdent(): List<PersonopplysningerMedIdent> =
        liste.filterIsInstance<PersonopplysningerMedIdent>()

    fun erTom(): Boolean = liste.isEmpty()

    fun harBrukerTilgang(bruker: Bruker): Boolean {
        return bruker is Systembruker || harTilgang(bruker as Saksbehandler)
    }

    fun sjekkBrukerTilgang(bruker: Bruker) {
        if (!harBrukerTilgang(bruker)) throw TilgangException("Bruker ${bruker.ident} har ikke tilgang til sak")
    }

    fun harTilgang(saksbehandler: Saksbehandler): Boolean {
        if (liste.any { it.strengtFortrolig() }) return Rolle.STRENGT_FORTROLIG_ADRESSE in saksbehandler.roller
        if (liste.any { it.fortrolig() }) return Rolle.FORTROLIG_ADRESSE in saksbehandler.roller
        if (liste.any { it.skjermet() }) return Rolle.SKJERMING in saksbehandler.roller
        return true
    }

    fun sjekkTilgang(saksbehandler: Saksbehandler) {
        if (!harTilgang(saksbehandler)) throw TilgangException("Saksbehandler ${saksbehandler.navIdent} har ikke tilgang til sak")
    }

    fun identerOgSkjerming(): Map<String, Boolean?> =
        personerMedIdent().associate {
            it.ident() to try {
                it.avklartSkjerming()
            } catch (e: IllegalStateException) {
                null
            }
        }

    fun personopplysningerMedSkjermingForIdent(ident: String, erSkjermet: Boolean?): SakPersonopplysninger {
        return SakPersonopplysninger(
            liste.map { personopplysninger ->
                when (personopplysninger) {
                    is PersonopplysningerBarnUtenIdent -> personopplysninger
                    is PersonopplysningerBarnMedIdent ->
                        if (personopplysninger.ident == ident) {
                            personopplysninger.copy(skjermet = erSkjermet)
                        } else {
                            personopplysninger
                        }

                    is PersonopplysningerSøker ->
                        if (personopplysninger.ident == ident) {
                            personopplysninger.copy(skjermet = erSkjermet)
                        } else {
                            personopplysninger
                        }
                }
            },
        )
    }

    fun medSkjermingFra(identerOgSkjerming: Map<String, Boolean?>): SakPersonopplysninger =
        identerOgSkjerming
            .toList()
            .fold(this) { personopplysninger: SakPersonopplysninger, personMedIdent: Pair<String, Boolean?> ->
                personopplysninger.personopplysningerMedSkjermingForIdent(personMedIdent.first, personMedIdent.second)
            }

    fun erLik(other: SakPersonopplysninger): Boolean {
        if (liste.size != other.liste.size) return false
        return liste.all { person ->
            other.liste.any { it == person }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SakPersonopplysninger
        return erLik(other)
    }

    override fun hashCode(): Int {
        return liste.hashCode()
    }
}
