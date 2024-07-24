package no.nav.tiltakspenger.saksbehandling.service.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Saksopplysning

interface BehandlingService {

    /**
     * Tenkt brukt i systemkall der vi ikke skal gjøre tilgangskontroll eller sjekk på skjermet/kode6/kode7
     * Eller der vi allerede har gjort tilgangskontroll.
     */
    fun hentBehandling(behandlingId: BehandlingId): Behandling

    /**
     * Tenkt brukt for kommandoer som er trigget av saksbehandler.
     * Her gjør vi en sjekk på om saksbehandler har skjermet/kode6/kode7 tilgang dersom det er relevant.
     * Vi gjør ikke tilgangskontroll utover dette. Det må gjøres i kallene som bruker denne metoden.
     */
    fun hentBehandling(behandlingId: BehandlingId, saksbehandler: Saksbehandler): Behandling
    fun hentAlleBehandlinger(saksbehandler: Saksbehandler): List<Førstegangsbehandling>
    fun leggTilSaksopplysning(behandlingId: BehandlingId, saksopplysning: Saksopplysning)
    fun leggTilSaksopplysning(behandling: Behandling, saksopplysning: Saksopplysning): Behandling
    fun sendTilBeslutter(behandlingId: BehandlingId, utøvendeSaksbehandler: Saksbehandler)
    fun sendTilbakeTilSaksbehandler(behandlingId: BehandlingId, utøvendeBeslutter: Saksbehandler, begrunnelse: String?)
    suspend fun iverksett(behandlingId: BehandlingId, utøvendeBeslutter: Saksbehandler)
    fun taBehandling(behandlingId: BehandlingId, utøvendeSaksbehandler: Saksbehandler)
    fun frataBehandling(behandlingId: BehandlingId, utøvendeSaksbehandler: Saksbehandler)
    fun hentBehandlingForIdent(ident: String, utøvendeSaksbehandler: Saksbehandler): List<Førstegangsbehandling>
    fun oppdaterAntallDagerPåTiltak(
        behandlingId: BehandlingId,
        tiltakId: TiltakId,
        periodeMedAntallDager: PeriodeMedVerdi<AntallDager>,
        saksbehandler: Saksbehandler,
    )

    fun tilbakestillAntallDagerPåTiltak(behandlingId: BehandlingId, tiltakId: TiltakId, saksbehandler: Saksbehandler)
}
