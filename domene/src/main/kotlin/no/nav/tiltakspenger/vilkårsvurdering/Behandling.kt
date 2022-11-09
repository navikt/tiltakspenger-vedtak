package no.nav.tiltakspenger.vilkårsvurdering

import no.nav.tiltakspenger.felles.BehandlingId

data class Behandling(
    val id: BehandlingId,
    val inngangsvilkårsvurderinger: Inngangsvilkårsvurderinger,
)
