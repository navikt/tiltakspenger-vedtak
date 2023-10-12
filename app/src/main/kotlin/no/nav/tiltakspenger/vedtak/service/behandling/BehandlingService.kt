package no.nav.tiltakspenger.vedtak.service.behandling

import no.nav.tiltakspenger.domene.behandling.Behandling
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.Søknad

interface BehandlingService {
    fun automatiskSaksbehandle(
        sakId: SakId,
        søknad: Søknad,
        saksopplysning: List<Saksopplysning>,
        saksbehandler: Saksbehandler,
    ): Behandling

    fun hentBehandling(behandlingId: BehandlingId): Søknadsbehandling?

    fun leggTilSaksopplysning(behandlingId: BehandlingId, saksopplysning: Saksopplysning)
}
