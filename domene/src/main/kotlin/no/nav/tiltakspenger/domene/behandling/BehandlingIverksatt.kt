package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Fakta
import no.nav.tiltakspenger.domene.vedtak.Vedtak
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

interface BehandlingIverksatt : Søknadsbehandling {
    val fakta: List<Fakta>
    val vilkårsvurderinger: List<Vurdering>
    val saksbehandler: Saksbehandler
    val vedtak: List<Vedtak>
    // TODO Trenger vi flere props/felter?

    data class Innvilget(
        override val id: BehandlingId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val fakta: List<Fakta>,
        override val vilkårsvurderinger: List<Vurdering>,
        override val saksbehandler: Saksbehandler,
        override val vedtak: List<Vedtak>,
    ) : BehandlingIverksatt {
        // trenger denne funksjoner?
    }

    data class Avslag(
        override val id: BehandlingId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val fakta: List<Fakta>,
        override val vilkårsvurderinger: List<Vurdering>,
        override val saksbehandler: Saksbehandler,
        override val vedtak: List<Vedtak>,
    ) : BehandlingIverksatt {
        // trenger denne funksjoner?
    }

    data class DelvisInnvilget(
        override val id: BehandlingId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val fakta: List<Fakta>,
        override val vilkårsvurderinger: List<Vurdering>,
        override val saksbehandler: Saksbehandler,
        override val vedtak: List<Vedtak>,
    ) : BehandlingIverksatt {
        // trenger denne funksjoner?
    }
}
