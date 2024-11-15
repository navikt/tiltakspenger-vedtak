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
import java.time.LocalDateTime

class BehandlingFakeRepo : BehandlingRepo {
    private val data = Atomic(mutableMapOf<BehandlingId, Førstegangsbehandling>())

    val alle get() = data.get().values.toList()

    override fun lagre(
        behandling: Behandling,
        transactionContext: TransactionContext?,
    ) {
        data.get()[behandling.id] = behandling as Førstegangsbehandling
    }

    override fun hentOrNull(
        behandlingId: BehandlingId,
        sessionContext: no.nav.tiltakspenger.libs.persistering.domene.SessionContext?,
    ): Behandling? = data.get()[behandlingId]

    override fun hent(
        behandlingId: BehandlingId,
        sessionContext: no.nav.tiltakspenger.libs.persistering.domene.SessionContext?,
    ): Behandling = hentOrNull(behandlingId, sessionContext)!!

    override fun hentAlleForIdent(fnr: Fnr): List<Førstegangsbehandling> =
        data
            .get()
            .values
            .filter { it.fnr == fnr }

    override fun hentForSøknadId(søknadId: SøknadId): Førstegangsbehandling? =
        data
            .get()
            .values
            .find { it.søknad.id == søknadId }

    override fun hentBehandlingerTilDatadeling(limit: Int): List<Førstegangsbehandling> {
        return data.get().values.filter {
            it.sendtTilDatadeling == null
        }
    }

    override fun markerSendtTilDatadeling(id: BehandlingId, tidspunkt: LocalDateTime) {
        data.get()[id] = data.get()[id]!!.copy(
            sendtTilDatadeling = tidspunkt,
        )
    }

    fun hentFørstegangsbehandlingForSakId(sakId: SakId): Førstegangsbehandling? =
        data
            .get()
            .values
            .find { it.sakId == sakId }
}
