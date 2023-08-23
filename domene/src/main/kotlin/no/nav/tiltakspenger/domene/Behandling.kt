package no.nav.tiltakspenger.domene

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad

data class Behandling(
    val id: BehandlingId,
    val søknader: List<Søknad>,
    val vurderingsperiode: Periode,
    val vedtak: List<Vedtak> = emptyList(),
) {
    companion object {
        fun lagBehandling(søknad: Søknad): Behandling {
            return Behandling(
                id = BehandlingId.random(),
                søknader = listOf(søknad),
                vurderingsperiode = søknad.vurderingsperiode(),
            )
        }
    }
}

//val innhentedeRådata: Innsending,
//val avklarteSaksopplysninger: List<Saksopplysning>,
//val vilkårsvurderinger: List<Vilkårsvurdering>,
