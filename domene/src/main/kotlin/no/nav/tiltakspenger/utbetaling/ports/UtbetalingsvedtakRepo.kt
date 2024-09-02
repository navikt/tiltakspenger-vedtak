package no.nav.tiltakspenger.utbetaling.ports

import no.nav.tiltakspenger.libs.common.BehandlingId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.VedtakId
import no.nav.tiltakspenger.meldekort.domene.UtfyltMeldekort
import no.nav.tiltakspenger.saksbehandling.ports.SendtUtbetaling
import no.nav.tiltakspenger.utbetaling.domene.Utbetalingsvedtak

interface UtbetalingsvedtakRepo {
    fun lagre(vedtak: Utbetalingsvedtak)

    fun markerUtbetalt(
        vedtakId: VedtakId,
        utbetalingsrespons: SendtUtbetaling,
    )

    fun hentForVedtakId(vedtakId: VedtakId): Utbetalingsvedtak?

    fun hentForSakId(sakId: SakId): List<Utbetalingsvedtak>

    fun hentForFørstegangsbehandlingId(behandlingId: BehandlingId): List<Utbetalingsvedtak>

    fun hentSisteUtbetalingsvedtak(sakId: SakId): Utbetalingsvedtak?

    fun hentGodkjenteMeldekortUtenUtbetalingsvedtak(limit: Int = 10): List<UtfyltMeldekort>

    fun hentUtbetalingsvedtakForUtsjekk(limit: Int = 10): List<Utbetalingsvedtak>
}