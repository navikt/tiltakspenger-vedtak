package no.nav.tiltakspenger.saksbehandling.service.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Behandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Førstegangsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Revurderingsbehandling
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.LivsoppholdSaksopplysning

interface BehandlingService {
    fun hentBehandlingOrNull(behandlingId: BehandlingId): Behandling?
    fun hentBehandling(behandlingId: BehandlingId): Behandling
    fun hentBehandlingForJournalpostId(journalpostId: String): Førstegangsbehandling?
    fun hentAlleBehandlinger(saksbehandler: Saksbehandler): List<Førstegangsbehandling>
    fun leggTilSaksopplysning(behandlingId: BehandlingId, livsoppholdSaksopplysning: LivsoppholdSaksopplysning)
    fun oppdaterTiltak(behandlingId: BehandlingId, tiltak: List<Tiltak>)
    fun sendTilBeslutter(behandlingId: BehandlingId, utøvendeSaksbehandler: Saksbehandler)
    fun sendTilbakeTilSaksbehandler(behandlingId: BehandlingId, utøvendeBeslutter: Saksbehandler, begrunnelse: String?)
    suspend fun iverksett(behandlingId: BehandlingId, utøvendeBeslutter: Saksbehandler)
    fun taBehandling(behandlingId: BehandlingId, utøvendeSaksbehandler: Saksbehandler)
    fun frataBehandling(behandlingId: BehandlingId, utøvendeSaksbehandler: Saksbehandler)
    fun hentBehandlingForIdent(ident: String, utøvendeSaksbehandler: Saksbehandler): List<Førstegangsbehandling>
    fun opprettRevurdering(behandlingId: BehandlingId, utøvendeSaksbehandler: Saksbehandler): Revurderingsbehandling
    fun oppdaterAntallDagerPåTiltak(
        behandlingId: BehandlingId,
        tiltakId: String,
        periodeMedAntallDager: PeriodeMedVerdi<AntallDager>,
        saksbehandler: Saksbehandler,
    )
}
