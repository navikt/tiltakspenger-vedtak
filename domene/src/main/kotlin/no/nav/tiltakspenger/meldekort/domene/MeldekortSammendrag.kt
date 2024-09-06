package no.nav.tiltakspenger.meldekort.domene

import no.nav.tiltakspenger.libs.common.MeldekortId
import no.nav.tiltakspenger.libs.periodisering.Periode

/**
 * @param erUtfylt dersom denne er i en liste, forventer vi kun at en ikke er utfylt.
 */
data class MeldekortSammendrag(
    val meldekortId: MeldekortId,
    val periode: Periode,
    val status: MeldekortStatus,
    val saksbehandler: String?,
    val beslutter: String?,
)
