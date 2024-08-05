package no.nav.tiltakspenger.saksbehandling.service.behandling

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.benk.Saksoversikt

interface BehandlingService {

    /**
     * Tenkt brukt i systemkall der vi ikke skal gjøre tilgangskontroll eller sjekk på skjermet/kode6/kode7
     * Eller der vi allerede har gjort tilgangskontroll.
     */
    fun hentBehandling(behandlingId: BehandlingId, sessionContext: SessionContext? = null): Behandling

    /**
     * Tenkt brukt for kommandoer som er trigget av saksbehandler.
     * Her gjør vi en sjekk på om saksbehandler har skjermet/kode6/kode7 tilgang dersom det er relevant.
     * Vi gjør ikke tilgangskontroll utover dette. Det må gjøres i kallene som bruker denne metoden.
     */
    fun hentBehandling(
        behandlingId: BehandlingId,
        saksbehandler: Saksbehandler,
        sessionContext: SessionContext? = null,
    ): Behandling

    fun hentBehandlingForSøknadId(søknadId: SøknadId): Førstegangsbehandling?
    fun hentSaksoversikt(saksbehandler: Saksbehandler): Saksoversikt
    fun sendTilBeslutter(behandlingId: BehandlingId, utøvendeSaksbehandler: Saksbehandler)
    fun sendTilbakeTilSaksbehandler(behandlingId: BehandlingId, utøvendeBeslutter: Saksbehandler, begrunnelse: String)
    suspend fun iverksett(behandlingId: BehandlingId, utøvendeBeslutter: Saksbehandler)
    fun taBehandling(behandlingId: BehandlingId, utøvendeSaksbehandler: Saksbehandler): Behandling

    fun frataBehandling(behandlingId: BehandlingId, utøvendeSaksbehandler: Saksbehandler)
}
