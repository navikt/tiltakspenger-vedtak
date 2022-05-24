package no.nav.tiltakspenger.domene.sak

import no.nav.tiltakspenger.domene.behandling.Saksbehandling
import no.nav.tiltakspenger.domene.Søknad

class Sak(
    val behandlinger: List<Saksbehandling>
) {
    companion object {
        fun opprettEllerHentSakMedBehandling(
            søknad: Søknad,
        ) : Sak {
            return Sak(emptyList())
        }
    }
}
