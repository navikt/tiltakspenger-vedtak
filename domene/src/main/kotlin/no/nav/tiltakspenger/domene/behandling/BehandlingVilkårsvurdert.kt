package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Fakta
import no.nav.tiltakspenger.domene.vedtak.Vedtak
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import java.util.UUID

sealed interface BehandlingVilkårsvurdert : Søknadsbehandling {
    val fakta: List<Fakta>
    val vilkårsvurderinger: List<Vurdering>

    data class Innvilget(
        override val id: BehandlingId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val fakta: List<Fakta>,
        override val vilkårsvurderinger: List<Vurdering>,
    ) : BehandlingVilkårsvurdert {
        fun iverksett(saksbehandler: Saksbehandler): BehandlingIverksatt.Innvilget {
            val vedtak = listOf(
                Vedtak(
                    id = UUID.randomUUID(),
                ),
            )
            // TODO Her må vi faktisk lage et skikkelig vedtak

            return BehandlingIverksatt.Innvilget(
                id = id,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                fakta = fakta,
                vilkårsvurderinger = vilkårsvurderinger,
                saksbehandler = saksbehandler,
                vedtak = vedtak,
            )
        }
    }

    data class Avslag(
        override val id: BehandlingId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val fakta: List<Fakta>,
        override val vilkårsvurderinger: List<Vurdering>,
    ) : BehandlingVilkårsvurdert {
        fun iverksett(saksbehandler: Saksbehandler): BehandlingIverksatt.Avslag {
            TODO()
        }
    }

    data class DelvisInnvilget(
        override val id: BehandlingId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val fakta: List<Fakta>,
        override val vilkårsvurderinger: List<Vurdering>,
    ) : BehandlingVilkårsvurdert {
        fun iverksett(saksbehandler: Saksbehandler): BehandlingIverksatt.DelvisInnvilget {
            TODO()
        }
    }

    data class Manuell(
        override val id: BehandlingId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val fakta: List<Fakta>,
        override val vilkårsvurderinger: List<Vurdering>,
    ) : BehandlingVilkårsvurdert {
//        fun vurderPåNytt(list: List<Fakta>): BehandlingVilkårsvurdert {
//            TODO()
//        }
    }
}
