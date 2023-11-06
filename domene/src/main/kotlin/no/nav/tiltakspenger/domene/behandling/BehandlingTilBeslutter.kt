package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vilkårsvurdering.Utfall
import no.nav.tiltakspenger.vilkårsvurdering.Vilkår
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

sealed interface BehandlingTilBeslutter : Søknadsbehandling {
    val vilkårsvurderinger: List<Vurdering>
    val saksbehandler: String

    override fun søknad(): Søknad {
        return søknader.maxBy { it.opprettet }
    }

    // Denne kan sikkert generaliseres for alle søknadsbehandlinger med vilkårsvurderinger
    fun hentUtfallForVilkår(vilkår: Vilkår): Utfall {
        if (vilkårsvurderinger.any { it.vilkår == vilkår && it.utfall == Utfall.KREVER_MANUELL_VURDERING }) return Utfall.KREVER_MANUELL_VURDERING
        if (vilkårsvurderinger.any { it.vilkår == vilkår && it.utfall == Utfall.IKKE_OPPFYLT }) return Utfall.IKKE_OPPFYLT
        if (vilkårsvurderinger.filter { it.vilkår == vilkår }.all { it.utfall == Utfall.OPPFYLT }) return Utfall.OPPFYLT
        throw IllegalStateException("Kunne ikke finne utfall for vilkår $vilkår")
    }

    fun sendTilbake(): BehandlingVilkårsvurdert

    companion object {
        fun fromDb(
            id: BehandlingId,
            sakId: SakId,
            søknader: List<Søknad>,
            vurderingsperiode: Periode,
            saksopplysninger: List<Saksopplysning>,
            tiltak: List<Tiltak>,
            vilkårsvurderinger: List<Vurdering>,
            status: String,
            saksbehandler: String,
        ): BehandlingTilBeslutter {
            when (status) {
                "Innvilget" -> return Innvilget(
                    id = id,
                    sakId = sakId,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    tiltak = tiltak,
                    vilkårsvurderinger = vilkårsvurderinger,
                    saksbehandler = saksbehandler,
                )

                "Avslag" -> return Avslag(
                    id = id,
                    sakId = sakId,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    tiltak = tiltak,
                    vilkårsvurderinger = vilkårsvurderinger,
                    saksbehandler = saksbehandler,
                )

                else -> throw IllegalStateException("Ukjent BehandlingTilBeslutting $id med status $status")
            }
        }
    }

    data class Innvilget(
        override val id: BehandlingId,
        override val sakId: SakId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,
        override val tiltak: List<Tiltak>,
        override val vilkårsvurderinger: List<Vurdering>,
        override val saksbehandler: String,
    ) : BehandlingTilBeslutter {
        fun iverksettAvBelutter(beslutter: String): BehandlingIverksatt.Innvilget {
            return BehandlingIverksatt.Innvilget(
                id = id,
                sakId = sakId,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                vilkårsvurderinger = vilkårsvurderinger,
                saksbehandler = saksbehandler,
                beslutter = beslutter,
            )
        }

        override fun sendTilbake(): BehandlingVilkårsvurdert.Innvilget {
            return BehandlingVilkårsvurdert.Innvilget(
                id = id,
                sakId = sakId,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                vilkårsvurderinger = vilkårsvurderinger,
            )
        }
    }

    data class Avslag(
        override val id: BehandlingId,
        override val sakId: SakId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,
        override val tiltak: List<Tiltak>,
        override val vilkårsvurderinger: List<Vurdering>,
        override val saksbehandler: String,
    ) : BehandlingTilBeslutter {
        fun iverksettAvBeslutter(beslutter: String): BehandlingIverksatt.Avslag {
            return BehandlingIverksatt.Avslag(
                id = id,
                sakId = sakId,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                vilkårsvurderinger = vilkårsvurderinger,
                saksbehandler = saksbehandler,
                beslutter = beslutter,
            )
        }

        override fun sendTilbake(): BehandlingVilkårsvurdert.Avslag {
            return BehandlingVilkårsvurdert.Avslag(
                id = id,
                sakId = sakId,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                tiltak = tiltak,
                vilkårsvurderinger = vilkårsvurderinger,
            )
        }
    }
}
