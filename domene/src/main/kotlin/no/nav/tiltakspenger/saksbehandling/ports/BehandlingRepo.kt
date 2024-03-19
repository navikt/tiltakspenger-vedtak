package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling

interface BehandlingRepo {
    fun lagre(behandling: Behandling): Behandling
    fun hentOrNull(behandlingId: BehandlingId): Behandling?
    fun hent(behandlingId: BehandlingId): Behandling
    fun hentAlle(): List<Førstegangsbehandling>
    fun hentAlleForIdent(ident: String): List<Førstegangsbehandling>
    fun hentForSak(sakId: SakId): List<Førstegangsbehandling>
    fun hentForJournalpostId(journalpostId: String): Førstegangsbehandling?
}
