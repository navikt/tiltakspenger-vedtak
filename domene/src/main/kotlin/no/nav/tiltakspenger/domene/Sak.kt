package no.nav.tiltakspenger.domene

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.vedtak.Søknad

data class Sak(
    val id: SakId,
    val saknummer: Saksnummer,
    val periode: Periode,
    val behandlinger: List<Behandling>,
) {
    fun håndter(søknad: Søknad): Sak {
        val behandling = Behandling.Opprettet.lagBehandling(søknad = søknad)

        return this.copy(
            behandlinger = behandlinger.plus(behandling),
        )
    }

    companion object {
        fun lagSak(søknad: Søknad, saksnummerGenerator: SaksnummerGenerator): Sak {

            val behandling = Behandling.Opprettet.lagBehandling(søknad = søknad)

            return Sak(
                id = SakId.random(),
                saknummer = saksnummerGenerator.genererSaknummer(),
                behandlinger = listOf(behandling),
                periode = behandling.vurderingsperiode,
            )
        }
    }


}
