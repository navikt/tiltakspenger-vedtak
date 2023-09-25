package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode

interface Behandling {
    val id: BehandlingId
    val vurderingsperiode: Periode
    val saksopplysninger: List<Saksopplysning>
}
