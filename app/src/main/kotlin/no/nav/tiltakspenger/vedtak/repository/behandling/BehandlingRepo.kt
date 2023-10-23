package no.nav.tiltakspenger.vedtak.repository.behandling

import no.nav.tiltakspenger.domene.behandling.Søknadsbehandling
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId

interface BehandlingRepo {
    fun lagre(behandling: Søknadsbehandling): Søknadsbehandling
    fun hent(behandlingId: BehandlingId): Søknadsbehandling?
    fun hentAlle(): List<Søknadsbehandling>
    fun hentForSak(sakId: SakId): List<Søknadsbehandling>
    fun hentForJournalpostId(journalpostId: String): Søknadsbehandling?
}
