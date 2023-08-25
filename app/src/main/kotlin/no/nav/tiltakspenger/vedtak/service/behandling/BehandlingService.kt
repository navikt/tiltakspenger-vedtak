package no.nav.tiltakspenger.vedtak.service.behandling

import no.nav.tiltakspenger.domene.behandling.Behandling
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.saksopplysning.Fakta
import no.nav.tiltakspenger.felles.Saksbehandler

interface BehandlingService {
    fun automatiskSaksbehandle(
        behandling: Søknadsbehandling.Opprettet,
        fakta: List<Fakta>,
        saksbehandler: Saksbehandler,
    ): Behandling
}
