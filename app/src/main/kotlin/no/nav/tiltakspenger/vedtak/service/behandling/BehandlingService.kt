package no.nav.tiltakspenger.vedtak.service.behandling

import no.nav.tiltakspenger.domene.behandling.Behandling
import no.nav.tiltakspenger.domene.saksopplysning.Fakta
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.Søknad

interface BehandlingService {
    fun automatiskSaksbehandle(
        //behandling: Søknadsbehandling.Opprettet,
        søknad: Søknad,
        fakta: List<Fakta>,
        saksbehandler: Saksbehandler,
    ): Behandling
}
