package no.nav.tiltakspenger.meldekort.domene

import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.periodisering.Periode

data class MeldekortSammendrag(
    val meldekortId: MeldekortId,
    val periode: Periode,
    val status: MeldekortStatus,
    val saksbehandler: String?,
    val beslutter: String?,
)
