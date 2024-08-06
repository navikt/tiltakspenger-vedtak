package no.nav.tiltakspenger.saksbehandling.ports

import arrow.core.NonEmptyList
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling

interface BehandlingRepo {
    fun lagre(
        behandling: Behandling,
        context: TransactionContext? = null,
    )

    fun hentOrNull(
        behandlingId: BehandlingId,
        sessionContext: SessionContext? = null,
    ): Behandling?

    fun hent(
        behandlingId: BehandlingId,
        sessionContext: SessionContext? = null,
    ): Behandling

    fun hentAlle(): List<Førstegangsbehandling>

    fun hentAlleForIdent(fnr: Fnr): List<Førstegangsbehandling>

    fun hentForSak(sakId: SakId): NonEmptyList<Førstegangsbehandling>

    fun hentForJournalpostId(journalpostId: String): Førstegangsbehandling?

    fun hentForSøknadId(søknadId: SøknadId): Førstegangsbehandling?
}
