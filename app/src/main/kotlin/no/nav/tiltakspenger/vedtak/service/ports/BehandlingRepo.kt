package no.nav.tiltakspenger.vedtak.service.ports

import kotliquery.TransactionalSession
import no.nav.tiltakspenger.domene.behandling.Behandling
import no.nav.tiltakspenger.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId

interface BehandlingRepo {
    fun lagre(behandling: Behandling): Behandling
    fun lagre(behandling: Behandling, tx: TransactionalSession): Behandling
    fun hent(behandlingId: BehandlingId): Behandling?
    fun hentAlle(): List<Førstegangsbehandling>
    fun hentAlleForIdent(ident: String): List<Førstegangsbehandling>
    fun hentForSak(sakId: SakId): List<Førstegangsbehandling>
    fun hentForJournalpostId(journalpostId: String): Førstegangsbehandling?
}
