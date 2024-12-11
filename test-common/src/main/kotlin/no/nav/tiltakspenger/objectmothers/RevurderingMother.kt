package no.nav.tiltakspenger.objectmothers

import arrow.core.nonEmptyListOf
import kotlinx.coroutines.runBlocking
import no.nav.tiltakspenger.common.TestApplicationContext
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.Saksbehandler
import no.nav.tiltakspenger.libs.common.getOrFail
import no.nav.tiltakspenger.libs.common.random
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.objectmothers.ObjectMother.beslutter
import no.nav.tiltakspenger.objectmothers.ObjectMother.saksbehandler
import no.nav.tiltakspenger.saksbehandling.domene.behandling.StartRevurderingKommando
import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.TiltakDeltakerstatus
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.felles.ÅrsakTilEndring
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltaksdeltagelse.LeggTilTiltaksdeltagelseKommando

object RevurderingMother {
    suspend fun TestApplicationContext.revurderingIverksatt(
        førstegangsbehandlingPeriode: Periode = ObjectMother.vurderingsperiode(),
        fnr: Fnr = Fnr.random(),
        saksbehandler: Saksbehandler = saksbehandler(),
        beslutter: Saksbehandler = beslutter(),
        correlationId: CorrelationId = CorrelationId.generate(),
        eksisterendeSak: Sak? = null,
        revurderingPeriode: Periode,
    ): Sak {
        val tac = this
        val sak = eksisterendeSak ?: førstegangsbehandlingIverksatt(
            periode = førstegangsbehandlingPeriode,
            fnr = fnr,
            saksbehandler = saksbehandler,
            beslutter = beslutter,
        )
        val sakId = sak.id
        runBlocking {
            val (_, revurdering) = tac.behandlingContext.startRevurderingService.startRevurdering(
                StartRevurderingKommando(
                    sakId = sakId,
                    periode = revurderingPeriode,
                    saksbehandler = saksbehandler,
                    correlationId = correlationId,
                ),
            ).getOrFail()
            val revurderingId = revurdering.id
            tac.behandlingContext.tiltaksdeltagelseVilkårService.oppdater(
                LeggTilTiltaksdeltagelseKommando(
                    sakId = sakId,
                    correlationId = correlationId,
                    saksbehandler = saksbehandler,
                    behandlingId = revurderingId,
                    statusForPeriode = nonEmptyListOf(
                        LeggTilTiltaksdeltagelseKommando.StatusForPeriode(
                            revurderingPeriode,
                            TiltakDeltakerstatus.HarSluttet,
                        ),
                    ),
                    årsakTilEndring = ÅrsakTilEndring.ENDRING_ETTER_SØKNADSTIDSPUNKT,
                ),
            ).getOrFail()
            tac.behandlingContext.behandlingService.sendTilBeslutter(
                behandlingId = revurderingId,
                saksbehandler = saksbehandler,
                correlationId = correlationId,
            ).getOrFail()
            tac.behandlingContext.behandlingService.taBehandling(
                behandlingId = revurderingId,
                saksbehandler = beslutter,
                correlationId = correlationId,
            ).getOrFail()
            tac.behandlingContext.behandlingService.iverksett(
                sakId = sakId,
                behandlingId = revurderingId,
                beslutter = beslutter,
                correlationId = correlationId,
            ).getOrFail()
        }
        return this.sakContext.sakService.hentForSakId(
            sakId,
            saksbehandler,
            correlationId,
        ).getOrFail()
    }
}
