package no.nav.tiltakspenger.saksbehandling.domene.sak

import no.nav.tiltakspenger.felles.SakId

interface SakDetaljer {
    val id: SakId
    val fnr: String
    val saksnummer: Saksnummer
}

data class TynnSak(
    override val id: SakId,
    override val fnr: String,
    override val saksnummer: Saksnummer,
) : SakDetaljer
