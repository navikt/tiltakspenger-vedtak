package no.nav.tiltakspenger.saksbehandling.domene.behandling

import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.libs.common.SakId
import no.nav.tiltakspenger.libs.common.Saksbehandler
import no.nav.tiltakspenger.libs.periodisering.Periode

data class StartRevurderingKommando(
    val sakId: SakId,
    val periode: Periode,
    val correlationId: CorrelationId,
    val saksbehandler: Saksbehandler,
)
