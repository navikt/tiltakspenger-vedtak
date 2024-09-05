package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling

interface BehandlingRepo {
    fun lagre(
        behandling: Behandling,
        transactionContext: TransactionContext? = null,
    )

    fun hentOrNull(
        behandlingId: BehandlingId,
        sessionContext: SessionContext? = null,
    ): Behandling?

    fun hent(
        behandlingId: BehandlingId,
        sessionContext: SessionContext? = null,
    ): Behandling

    fun hentAlleForIdent(fnr: Fnr): List<Førstegangsbehandling>

    fun hentForSøknadId(søknadId: SøknadId): Førstegangsbehandling?
}
