package no.nav.tiltakspenger.saksbehandling.domene.sak

import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.SakId

interface SakDetaljer {
    val id: SakId
    val fnr: Fnr
    val saksnummer: Saksnummer
}

data class TynnSak(
    override val id: SakId,
    override val fnr: Fnr,
    override val saksnummer: Saksnummer,
) : SakDetaljer
