package no.nav.tiltakspenger.vedtak.repository.behandling

import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling

interface BehandlingRepo {
    fun lagre(behandling: Søknadsbehandling)
}
