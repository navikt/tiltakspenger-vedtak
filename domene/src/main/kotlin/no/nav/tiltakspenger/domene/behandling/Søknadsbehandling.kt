package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Fakta
import no.nav.tiltakspenger.domene.saksopplysning.lagVurdering
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
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
            val vurderinger = fakta.filterIsInstance<Fakta.Aap>().lagVurdering(
                Fakta.Aap(
                    fom = vurderingsperiode.fra,
                    tom = vurderingsperiode.til,
                    vilkår = Vilkår.AAP,
                    kilde = "Arena",
                    detaljer = "",
                ),
            ) + fakta.filterIsInstance<Fakta.Dagpenger>().lagVurdering(
                    Fakta.Dagpenger(
                        fom = vurderingsperiode.fra,
                        tom = vurderingsperiode.til,
                        vilkår = Vilkår.DAGPENGER,
                        kilde = "Arena",
                        detaljer = "",
                    ),
                )

            if (vurderinger.all{it.utfall == Utfall.OPPFYLT}) {
                return BehandlingVilkårsvurdert.Innvilget(
                    id = id,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    fakta = fakta,
                    vilkårsvurderinger = vurderinger,
                )
            }
            if (vurderinger.any{it.utfall == Utfall.IKKE_OPPFYLT}) {
                return BehandlingVilkårsvurdert.Avslag(
                    id = id,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    fakta = fakta,
                    vilkårsvurderinger = vurderinger,
                )
            }
            // TODO Delvis og Manuell. hva gjør vi med de?
            return BehandlingVilkårsvurdert.Manuell(
                id = id,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                fakta = fakta,
                vilkårsvurderinger = vurderinger,
            )
        }
    }
}
