package no.nav.tiltakspenger.saksbehandling.domene.sak

import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.libs.periodisering.Periode

interface SakDetaljer {
    val id: SakId
    val ident: String
    val saknummer: Saksnummer
    val periode: Periode
}

data class TynnSak(
    override val id: SakId,
    override val ident: String,
    override val saknummer: Saksnummer,
    override val periode: Periode,
) : SakDetaljer
