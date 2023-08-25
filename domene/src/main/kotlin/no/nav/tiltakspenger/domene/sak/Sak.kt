package no.nav.tiltakspenger.domene.sak

import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.vedtak.Søknad

data class Sak(
    val id: SakId,
    val saknummer: Saksnummer,
    val periode: Periode,
    val behandlinger: List<Søknadsbehandling>,
) {
    fun håndter(søknad: Søknad): Sak {
        val behandling = Søknadsbehandling.Opprettet.opprettBehandling(søknad = søknad)

        return this.copy(
            behandlinger = behandlinger.plus(behandling),
        )
    }

    companion object {
        fun lagSak(søknad: Søknad, saksnummerGenerator: SaksnummerGenerator): Sak {
            val behandling = Søknadsbehandling.Opprettet.opprettBehandling(søknad = søknad)

            return Sak(
                id = SakId.random(),
                saknummer = saksnummerGenerator.genererSaknummer(),
                behandlinger = listOf(behandling),
                periode = behandling.vurderingsperiode,
            )
        }
    }
}
