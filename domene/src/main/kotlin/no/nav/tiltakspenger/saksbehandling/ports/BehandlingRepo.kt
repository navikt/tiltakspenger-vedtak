package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling

interface BehandlingRepo {
    fun lagre(behandling: Behandling, context: TransactionContext? = null)
    fun hentOrNull(behandlingId: BehandlingId, sessionContext: SessionContext? = null): Behandling?
    fun hent(behandlingId: BehandlingId, sessionContext: SessionContext? = null): Behandling
    fun hentAlle(): List<Førstegangsbehandling>
    fun hentAlleForIdent(ident: String): List<Førstegangsbehandling>
    fun hentForSak(sakId: SakId): List<Førstegangsbehandling>
    fun hentForJournalpostId(journalpostId: String): Førstegangsbehandling?
    fun hentForSøknadId(søknadId: SøknadId): Førstegangsbehandling?
}
