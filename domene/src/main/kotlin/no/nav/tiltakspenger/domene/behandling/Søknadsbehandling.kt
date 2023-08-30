package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Fakta
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

sealed interface Søknadsbehandling : Behandling {
    data class Opprettet(
        override val id: BehandlingId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
    ) : Søknadsbehandling {
        // TODO Vurder om vi skal ha behandlingService som er ansvarlig for å opprette denne,
        //      eller om vi skal beholde denne (eller begge :-) )
        companion object {
            fun opprettBehandling(søknad: Søknad): Søknadsbehandling.Opprettet {
                return Opprettet(
                    id = BehandlingId.random(),
                    søknader = listOf(søknad),
                    vurderingsperiode = søknad.vurderingsperiode(),
                )
            }
        }

        fun vilkårsvurder(fakta: List<Fakta>): BehandlingVilkårsvurdert {
            // TODO gjør en vilkårsvurdering og returner riktig type (Innvilget/Avslag/Manuell)
            val vilkårsvurderinger = emptyList<Vurdering>()

            return BehandlingVilkårsvurdert.Innvilget(
                id = id,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                fakta = fakta,
                vilkårsvurderinger = vilkårsvurderinger,
            )
        }
    }
}
