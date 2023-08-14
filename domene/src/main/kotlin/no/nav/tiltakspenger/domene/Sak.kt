package no.nav.tiltakspenger.domene

import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.vedtak.Søknad

data class Sak(
    val id: SakId,
    val saknummer: Saksnummer,
    val behandlinger: List<Behandling>,
) {
    fun håndter(søknad: Søknad): Sak {
        TODO("Not yet implemented")
    }

    companion object {
        fun lagSak(søknad: Søknad, saksnummerGenerator: SaksnummerGenerator): Sak {

            val behandling = Behandling.lagBehandling(søknad = søknad)

            return Sak(
                id = SakId.random(),
                saknummer = saksnummerGenerator.genererSaknummer(),
                behandlinger = listOf(behandling),
            )
        }
    }
}
