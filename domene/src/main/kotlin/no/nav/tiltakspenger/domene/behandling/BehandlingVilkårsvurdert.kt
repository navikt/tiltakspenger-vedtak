package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.vedtak.Vedtak
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering
import java.util.UUID

sealed interface BehandlingVilkårsvurdert : Søknadsbehandling {
    val saksopplysning: List<Saksopplysning>
    val vilkårsvurderinger: List<Vurdering>

    fun vurderPåNytt(list: List<Saksopplysning>): BehandlingVilkårsvurdert

    data class Innvilget(
        override val id: BehandlingId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val saksopplysning: List<Saksopplysning>,
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
                saksopplysning = saksopplysning,
                vilkårsvurderinger = vilkårsvurderinger,
                saksbehandler = saksbehandler,
                vedtak = vedtak,
            )
        }

        override fun vurderPåNytt(list: List<Saksopplysning>): BehandlingVilkårsvurdert {
            TODO()
        }
    }

    data class Avslag(
        override val id: BehandlingId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val saksopplysning: List<Saksopplysning>,
        override val vilkårsvurderinger: List<Vurdering>,
    ) : BehandlingVilkårsvurdert {
        fun iverksett(saksbehandler: Saksbehandler): BehandlingIverksatt.Avslag {
            TODO()
        }

        override fun vurderPåNytt(list: List<Saksopplysning>): BehandlingVilkårsvurdert {
            TODO()
        }
    }

    data class DelvisInnvilget(
        override val id: BehandlingId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val saksopplysning: List<Saksopplysning>,
        override val vilkårsvurderinger: List<Vurdering>,
    ) : BehandlingVilkårsvurdert {
        fun iverksett(saksbehandler: Saksbehandler): BehandlingIverksatt.DelvisInnvilget {
            TODO()
        }

        override fun vurderPåNytt(list: List<Saksopplysning>): BehandlingVilkårsvurdert {
            TODO()
        }
    }

    data class Manuell(
        override val id: BehandlingId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val saksopplysning: List<Saksopplysning>,
        override val vilkårsvurderinger: List<Vurdering>,
    ) : BehandlingVilkårsvurdert {
        override fun vurderPåNytt(list: List<Saksopplysning>): BehandlingVilkårsvurdert {
            TODO()
        }
    }
}
