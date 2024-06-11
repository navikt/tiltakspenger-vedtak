package no.nav.tiltakspenger.saksbehandling.domene.vilkår

import no.nav.tiltakspenger.libs.periodisering.Periodisering

data class Vurdering(
    val vilkår: Vilkår,
    val utfall: Periodisering<Utfall>,
    val detaljer: String,
)
