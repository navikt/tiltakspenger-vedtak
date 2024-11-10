package no.nav.tiltakspenger.objectmothers

import arrow.core.nonEmptyListOf
import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.felles.Systembrukerrolle
import no.nav.tiltakspenger.felles.Systembrukerroller

interface SystembrukerMother {
    fun systembrukerHenteData() = Systembruker(
        brukernavn = "systembrukerHenteData",
        roller = Systembrukerroller(nonEmptyListOf(Systembrukerrolle.HENTE_DATA)),
    )

    fun systembrukerLageHendelser() = Systembruker(
        brukernavn = "systembrukerLageHendelser",
        roller = Systembrukerroller(nonEmptyListOf(Systembrukerrolle.LAGE_HENDELSER)),
    )
}
