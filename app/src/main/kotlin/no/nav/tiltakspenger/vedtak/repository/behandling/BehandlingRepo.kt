package no.nav.tiltakspenger.vedtak.repository.behandling

import no.nav.tiltakspenger.domene.behandling.Behandling
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.felles.BehandlingId

interface BehandlingRepo {
    fun lagre(behandling: Søknadsbehandling)
    fun hent(behandlingId: BehandlingId): Behandling
}
