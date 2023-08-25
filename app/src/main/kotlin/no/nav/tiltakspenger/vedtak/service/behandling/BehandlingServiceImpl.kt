package no.nav.tiltakspenger.vedtak.service.behandling

import no.nav.tiltakspenger.domene.behandling.Behandling
import no.nav.tiltakspenger.domene.behandling.BehandlingVilkårsvurdert
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.saksopplysning.Fakta
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.repository.behandling.BehandlingRepo

class BehandlingServiceImpl(
    val behandlingRepo: BehandlingRepo,
) : BehandlingService {
    override fun automatiskSaksbehandle(
        behandling: Søknadsbehandling.Opprettet,
        fakta: List<Fakta>,
        saksbehandler: Saksbehandler,
    ): Behandling {
        val behandlingVilkårsvurdert = behandling.vilkårsvurder(
            fakta = fakta,
        )

        return when (behandlingVilkårsvurdert) {
            is BehandlingVilkårsvurdert.Avslag -> behandlingVilkårsvurdert.iverksett(saksbehandler)
            is BehandlingVilkårsvurdert.DelvisInnvilget -> behandlingVilkårsvurdert.iverksett(saksbehandler)
            is BehandlingVilkårsvurdert.Innvilget -> behandlingVilkårsvurdert.iverksett(saksbehandler)
            is BehandlingVilkårsvurdert.Manuell -> return behandlingVilkårsvurdert
        }
    }
}
