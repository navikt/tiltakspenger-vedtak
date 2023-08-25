package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad

interface Behandling {
    val id: BehandlingId
    val søknader: List<Søknad>
    val vurderingsperiode: Periode
}
