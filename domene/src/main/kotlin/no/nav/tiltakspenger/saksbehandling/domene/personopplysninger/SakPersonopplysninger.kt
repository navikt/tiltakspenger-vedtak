package no.nav.tiltakspenger.saksbehandling.domene.personopplysninger

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.Rolle

data class SakPersonopplysninger(
    // TODO jah: Midlertidig public, mens vi skriver oss bort fra RnR.
    val liste: List<Personopplysninger> = emptyList(),
) {
    fun søkere(): List<PersonopplysningerSøker> = liste.filterIsInstance<PersonopplysningerSøker>()

    fun søkerOrNull(): PersonopplysningerSøker? = liste.filterIsInstance<PersonopplysningerSøker>().firstOrNull()

    fun søkerMedIdent(fnr: Fnr): PersonopplysningerSøker? = liste.filterIsInstance<PersonopplysningerSøker>().firstOrNull { it.fnr == fnr }

    fun søker(): PersonopplysningerSøker = liste.filterIsInstance<PersonopplysningerSøker>().first()

    fun barnMedIdent(): List<PersonopplysningerBarnMedIdent> = liste.filterIsInstance<PersonopplysningerBarnMedIdent>()

    fun barnMedIdent(fnr: Fnr): PersonopplysningerBarnMedIdent? =
        liste.filterIsInstance<PersonopplysningerBarnMedIdent>().firstOrNull { it.fnr == fnr }

    fun barnUtenIdent(): List<PersonopplysningerBarnUtenIdent> = liste.filterIsInstance<PersonopplysningerBarnUtenIdent>()

    fun personerMedIdent(): List<PersonopplysningerMedIdent> = liste.filterIsInstance<PersonopplysningerMedIdent>()

    fun erTom(): Boolean = liste.isEmpty()

    fun harTilgang(saksbehandler: Saksbehandler): Boolean {
        if (liste.any { it.strengtFortrolig() }) return Rolle.STRENGT_FORTROLIG_ADRESSE in saksbehandler.roller
        if (liste.any { it.fortrolig() }) return Rolle.FORTROLIG_ADRESSE in saksbehandler.roller
        if (liste.any { it.skjermet() }) return Rolle.SKJERMING in saksbehandler.roller
        return true
    }

    fun identerOgSkjerming(): Map<Fnr, Boolean?> =
        personerMedIdent().associate {
            it.fnr() to
                try {
                    it.avklartSkjerming()
                } catch (e: IllegalStateException) {
                    null
                }
        }

    fun personopplysningerMedSkjermingForIdent(
        fnr: Fnr,
        erSkjermet: Boolean?,
    ): SakPersonopplysninger =
        SakPersonopplysninger(
            liste.map { personopplysninger ->
                when (personopplysninger) {
                    is PersonopplysningerBarnUtenIdent -> personopplysninger
                    is PersonopplysningerBarnMedIdent ->
                        if (personopplysninger.fnr == fnr) {
                            personopplysninger.copy(skjermet = erSkjermet)
                        } else {
                            personopplysninger
                        }

                    is PersonopplysningerSøker ->
                        if (personopplysninger.fnr == fnr) {
                            personopplysninger.copy(skjermet = erSkjermet)
                        } else {
                            personopplysninger
                        }
                }
            },
        )

    fun medSkjermingFra(identerOgSkjerming: Map<Fnr, Boolean?>): SakPersonopplysninger =
        identerOgSkjerming
            .toList()
            .fold(this) { personopplysninger: SakPersonopplysninger, personMedIdent: Pair<Fnr, Boolean?> ->
                personopplysninger.personopplysningerMedSkjermingForIdent(personMedIdent.first, personMedIdent.second)
            }

    fun erLik(other: SakPersonopplysninger): Boolean {
        if (liste.size != other.liste.size) return false
        return liste.all { person ->
            other.liste.any { it == person }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (javaClass != other.javaClass) return false

        other as SakPersonopplysninger
        return erLik(other)
    }

    override fun hashCode(): Int = liste.hashCode()
}
