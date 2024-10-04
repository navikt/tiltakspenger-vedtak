package no.nav.tiltakspenger.meldekort.domene

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.common.CorrelationId
import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.common.SakId

data class IverksettMeldekortKommando(
    val sakId: SakId,
    val meldekortId: MeldekortId,
    val beslutter: Saksbehandler,
    val correlationId: CorrelationId,
)
