package no.nav.tiltakspenger.saksbehandling.service.sak

import arrow.core.Either
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.SøknadId
import no.nav.tiltakspenger.saksbehandling.domene.behandling.KanIkkeOppretteBehandling
import no.nav.tiltakspenger.saksbehandling.domene.benk.Saksoversikt
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.sak.Saksnummer
import no.nav.tiltakspenger.saksbehandling.service.sak.SakServiceImpl.FantIkkeFnr

interface SakService {
    suspend fun startFørstegangsbehandling(
        søknadId: SøknadId,
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
    ): Either<KanIkkeStarteFørstegangsbehandling, Sak>

    suspend fun hentForFørstegangsbehandlingId(
        behandlingId: BehandlingId,
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
    ): Sak

    suspend fun hentForSaksnummer(
        saksnummer: Saksnummer,
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
    ): Sak

    suspend fun hentForFnr(
        fnr: Fnr,
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
    ): Either<FantIkkeFnr, Sak>

    fun hentFnrForSakId(sakId: SakId): Fnr?

    suspend fun hentForSakId(
        sakId: SakId,
        saksbehandler: Saksbehandler,
        correlationId: CorrelationId,
    ): Sak?

    fun hentSaksoversikt(saksbehandler: Saksbehandler): Saksoversikt
}

sealed interface KanIkkeStarteFørstegangsbehandling {
    data class HarAlleredeStartetBehandlingen(
        val behandlingId: BehandlingId,
    ) : KanIkkeStarteFørstegangsbehandling

    data class OppretteBehandling(
        val underliggende: KanIkkeOppretteBehandling,
    ) : KanIkkeStarteFørstegangsbehandling
}
