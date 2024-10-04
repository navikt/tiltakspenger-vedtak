package no.nav.tiltakspenger.saksbehandling.service.behandling

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import java.time.LocalDate

interface BehandlingService {
    /**
     * Tenkt brukt i systemkall der vi ikke skal gjøre tilgangskontroll eller sjekk på skjermet/kode6/kode7
     * Eller der vi allerede har gjort tilgangskontroll.
     */
    fun hentBehandling(
        behandlingId: BehandlingId,
        sessionContext: SessionContext? = null,
    ): Behandling

    /**
     * Tenkt brukt for kommandoer som er trigget av saksbehandler.
     * Her gjør vi en sjekk på om saksbehandler har skjermet/kode6/kode7 tilgang dersom det er relevant.
     * Vi gjør ikke tilgangskontroll utover dette. Det må gjøres i kallene som bruker denne metoden.
     */
    suspend fun hentBehandling(
        behandlingId: BehandlingId,
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
        sessionContext: SessionContext? = null,
    ): Behandling

    suspend fun sendTilBeslutter(
        behandlingId: BehandlingId,
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
    )

    suspend fun sendTilbakeTilSaksbehandler(
        behandlingId: BehandlingId,
        beslutter: Saksbehandler,
        begrunnelse: String,
        correlationId: CorrelationId,
    )

    suspend fun iverksett(
        behandlingId: BehandlingId,
        beslutter: Saksbehandler,
        correlationId: CorrelationId,
    )

    suspend fun taBehandling(
        behandlingId: BehandlingId,
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
    ): Behandling

    fun hentBehandlingerUnderBehandlingForIdent(
        ident: Fnr,
        fom: LocalDate,
        tom: LocalDate,
    ): List<Behandling>
}
