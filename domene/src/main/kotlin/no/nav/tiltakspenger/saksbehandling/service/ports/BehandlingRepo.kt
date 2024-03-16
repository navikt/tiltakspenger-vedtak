package no.nav.tiltakspenger.saksbehandling.service.ports

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling

interface BehandlingRepo {
    fun lagre(behandling: Førstegangsbehandling): Førstegangsbehandling

    // fun lagre(behandling: Førstegangsbehandling, tx: TransactionalSession): Førstegangsbehandling
    fun hentOrNull(behandlingId: BehandlingId): Førstegangsbehandling?
    fun hent(behandlingId: BehandlingId): Førstegangsbehandling
    fun hentAlle(): List<Førstegangsbehandling>
    fun hentAlleForIdent(ident: String): List<Førstegangsbehandling>
    fun hentForSak(sakId: SakId): List<Førstegangsbehandling>
    fun hentForJournalpostId(journalpostId: String): Førstegangsbehandling?
}
