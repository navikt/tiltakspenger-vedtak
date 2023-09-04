package no.nav.tiltakspenger.domene.sak

import no.nav.tiltakspenger.domene.behandling.Behandling
import no.nav.tiltakspenger.domene.behandling.BehandlingIverksatt
import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
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
        val behandling = Søknadsbehandling.Opprettet.opprettBehandling(søknad = søknad)

        return this.copy(
            behandlinger = behandlinger.plus(behandling),
        )
    }

    fun mottaFakta(saksopplysning: List<Saksopplysning>): Sak {
        val behandlinger = behandlinger.filterIsInstance<Søknadsbehandling>().map { behandling ->
            when (behandling) {
                is Søknadsbehandling.Opprettet -> behandling.vilkårsvurder(saksopplysning)
                is BehandlingVilkårsvurdert.Manuell -> behandling.vurderPåNytt(saksopplysning)
                is BehandlingVilkårsvurdert.Avslag -> throw RuntimeException("kan ikke endre saksopplysninger til en Behandling som er Avslag")
                is BehandlingVilkårsvurdert.DelvisInnvilget -> throw RuntimeException("kan ikke endre saksopplysninger til en Behandling som er Delvis Innvilget")
                is BehandlingVilkårsvurdert.Innvilget -> throw RuntimeException("kan ikke endre saksopplysninger til en Behandling som er Innvilget")
                is BehandlingIverksatt -> behandling
            }
        }

        return this.copy(
            behandlinger = behandlinger,
        )
    }

    companion object {
        fun lagSak(søknad: Søknad, saksnummerGenerator: SaksnummerGenerator): Sak {
            return Sak(
                id = SakId.random(),
                saknummer = saksnummerGenerator.genererSaknummer(),
                behandlinger = emptyList(),
                periode = søknad.vurderingsperiode(),
            )
        }
    }
}
