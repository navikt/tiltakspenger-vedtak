package no.nav.tiltakspenger.saksbehandling.ports

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.felles.VedtakId
import no.nav.tiltakspenger.saksbehandling.domene.vedtak.Vedtak

interface VedtakRepo {
    fun hent(vedtakId: VedtakId): Vedtak?
    fun hentVedtakForBehandling(behandlingId: BehandlingId): List<Vedtak>
    fun hentVedtakForSak(sakId: SakId): List<Vedtak>
    fun lagreVedtak(vedtak: Vedtak): Vedtak
}
