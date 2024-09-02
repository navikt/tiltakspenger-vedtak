package no.nav.tiltakspenger.meldekort.domene

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.common.MeldekortId

data class IverksettMeldekortKommando(
    val meldekortId: MeldekortId,
    val beslutter: Saksbehandler,
)
