package no.nav.tiltakspenger.vedtak.repository.behandling

import no.nav.tiltakspenger.domene.behandling.Behandling
import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.objectmothers.ObjectMother

class PostgresBehandlingRepo() : BehandlingRepo {
    override fun lagre(behandling: Søknadsbehandling) {
    }

    override fun hent(behandlingId: BehandlingId): Behandling {
        // TODO: Denne skal ikke opprette behandling på sikt, men skal hente ut fra databasen.
        return Søknadsbehandling.Opprettet.opprettBehandling(
            søknad = ObjectMother.nySøknadMedTiltak(),
        )
    }
}
