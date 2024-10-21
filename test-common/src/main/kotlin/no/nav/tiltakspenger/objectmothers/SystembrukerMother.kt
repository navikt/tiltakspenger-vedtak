package no.nav.tiltakspenger.objectmothers

import arrow.core.nonEmptyListOf
import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.libs.common.Rolle
import no.nav.tiltakspenger.libs.common.Roller

interface SystembrukerMother {
    fun systembrukerHenteData() = Systembruker(
        brukernavn = "systembrukerHenteData",
        roller = Roller(nonEmptyListOf(Rolle.HENTE_DATA)),
    )

    fun systembrukerLageHendelser() = Systembruker(
        brukernavn = "systembrukerLageHendelser",
        roller = Roller(nonEmptyListOf(Rolle.LAGE_HENDELSER)),
    )
}
