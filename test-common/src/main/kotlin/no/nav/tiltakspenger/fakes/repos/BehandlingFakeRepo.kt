package no.nav.tiltakspenger.fakes.repos

import arrow.atomic.Atomic
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.ports.BehandlingRepo

class BehandlingFakeRepo : BehandlingRepo {

    private val data = Atomic(mutableMapOf<BehandlingId, Behandling>())

    val alle get() = data.get().values.toList()

    override fun lagre(
        behandling: Behandling,
        transactionContext: TransactionContext?,
    ) {
        data.get()[behandling.id] = behandling
    }

    override fun hentOrNull(
        behandlingId: BehandlingId,
        sessionContext: no.nav.tiltakspenger.libs.persistering.domene.SessionContext?,
    ): Behandling? {
        return data.get()[behandlingId]
    }

    override fun hent(
        behandlingId: BehandlingId,
        sessionContext: no.nav.tiltakspenger.libs.persistering.domene.SessionContext?,
    ): Behandling {
        return hentOrNull(behandlingId, sessionContext)!!
    }

    override fun hentAlleForIdent(fnr: Fnr): List<Førstegangsbehandling> {
        return data.get().values
            .filterIsInstance<Førstegangsbehandling>()
            .filter { it.fnr == fnr }
    }

    override fun hentForSøknadId(søknadId: SøknadId): Førstegangsbehandling? {
        return data.get().values
            .filterIsInstance<Førstegangsbehandling>()
            .find { it.søknad.id == søknadId }
    }

    fun hentFørstegangsbehandlingForSakId(sakId: SakId): Førstegangsbehandling? {
        return data.get().values
            .filterIsInstance<Førstegangsbehandling>()
            .find { it.sakId == sakId }
    }
}
