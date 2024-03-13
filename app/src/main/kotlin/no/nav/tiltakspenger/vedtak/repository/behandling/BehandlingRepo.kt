package no.nav.tiltakspenger.vedtak.repository.behandling

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId

interface BehandlingRepo {
    fun lagre(behandling: Førstegangsbehandling): Førstegangsbehandling
    fun lagre(behandling: Førstegangsbehandling, tx: TransactionalSession): Førstegangsbehandling
    fun hent(behandlingId: BehandlingId): Førstegangsbehandling?
    fun hentAlle(): List<Førstegangsbehandling>
    fun hentAlleForIdent(ident: String): List<Førstegangsbehandling>
    fun hentForSak(sakId: SakId): List<Førstegangsbehandling>
    fun hentForJournalpostId(journalpostId: String): Førstegangsbehandling?
}
