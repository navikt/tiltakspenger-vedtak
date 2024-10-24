package no.nav.tiltakspenger.saksbehandling.service.behandling

import arrow.core.Either
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.Systembruker
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Rammevedtak

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
    ): Either<KanIkkeSendeBehandlingTilBeslutter, Behandling>

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
    ): Either<KanIkkeIverksetteBehandling, Rammevedtak>

    suspend fun taBehandling(
        behandlingId: BehandlingId,
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
    ): Either<KanIkkeTaBehandling, Behandling>

    fun hentBehandlingerUnderBehandlingForIdent(
        fnr: Fnr,
        periode: Periode,
        systembruker: Systembruker,
    ): List<Behandling>
}

sealed interface KanIkkeSendeBehandlingTilBeslutter {
    data object BehandlingKanIkkeVæreUavklart : KanIkkeSendeBehandlingTilBeslutter
    data object StøtterIkkeDelvisEllerAvslag : KanIkkeSendeBehandlingTilBeslutter
}
sealed interface KanIkkeIverksetteBehandling {
    data object StøtterIkkeDelvisEllerAvslag : KanIkkeIverksetteBehandling
}
sealed interface KanIkkeTaBehandling {
    data object MåVæreSaksbehandler : KanIkkeTaBehandling
    data object MåVæreBeslutter : KanIkkeTaBehandling
}
