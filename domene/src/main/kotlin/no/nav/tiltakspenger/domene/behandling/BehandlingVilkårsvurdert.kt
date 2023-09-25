package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.Innsending
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

sealed interface BehandlingVilkårsvurdert : Søknadsbehandling {
    //    val saksopplysning: List<Saksopplysning>
    val vilkårsvurderinger: List<Vurdering>

    data class Innvilget(
        override val id: BehandlingId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,
        override val vilkårsvurderinger: List<Vurdering>,
        override val innsending: Innsending?,
    ) : BehandlingVilkårsvurdert {
        fun iverksett(saksbehandler: Saksbehandler): BehandlingIverksatt.Innvilget {
            return BehandlingIverksatt.Innvilget(
                id = id,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                innsending = innsending,
                saksopplysninger = saksopplysninger,
                vilkårsvurderinger = vilkårsvurderinger,
                saksbehandler = saksbehandler,
            )
        }
    }

    data class Avslag(
        override val id: BehandlingId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,
        override val vilkårsvurderinger: List<Vurdering>,
        override val innsending: Innsending?,
    ) : BehandlingVilkårsvurdert {
        fun iverksett(saksbehandler: Saksbehandler): BehandlingIverksatt.Avslag {
            TODO()
        }
    }

    data class DelvisInnvilget(
        override val id: BehandlingId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,
        override val vilkårsvurderinger: List<Vurdering>,
        override val innsending: Innsending?,
    ) : BehandlingVilkårsvurdert {
        fun iverksett(saksbehandler: Saksbehandler): BehandlingIverksatt.DelvisInnvilget {
            TODO()
        }
    }

    data class Manuell(
        override val id: BehandlingId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,
        override val vilkårsvurderinger: List<Vurdering>,
        override val innsending: Innsending?,
    ) : BehandlingVilkårsvurdert {
        fun vurderPåNytt(saksopplysninger: List<Saksopplysning>): BehandlingVilkårsvurdert {
            return Søknadsbehandling.Opprettet(
                id = id,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                innsending = innsending,
            ).vilkårsvurder(saksopplysninger)
        }
    }
}
