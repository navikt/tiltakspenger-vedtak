package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

sealed interface BehandlingVilkårsvurdert : Søknadsbehandling {
    val vilkårsvurderinger: List<Vurdering>

    override fun søknad(): Søknad {
        return søknader.maxBy { it.opprettet }
    }

    companion object {
        fun fromDb(
            id: BehandlingId,
            søknader: List<Søknad>,
            vurderingsperiode: Periode,
            saksopplysninger: List<Saksopplysning>,
            vilkårsvurderinger: List<Vurdering>,
            status: String,
        ): BehandlingVilkårsvurdert {
            when (status) {
                "Innvilget" -> return Innvilget(
                    id = id,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    vilkårsvurderinger = vilkårsvurderinger,
                )

                "Avslag" -> return Avslag(
                    id = id,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    vilkårsvurderinger = vilkårsvurderinger,
                )

                "Manuell" -> return Manuell(
                    id = id,
                    søknader = søknader,
                    vurderingsperiode = vurderingsperiode,
                    saksopplysninger = saksopplysninger,
                    vilkårsvurderinger = vilkårsvurderinger,
                )

                else -> throw IllegalStateException("Ukjent BehandlingVilkårsvurdert $id med status $status")
            }
        }
    }

    data class Innvilget(
        override val id: BehandlingId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,
        override val vilkårsvurderinger: List<Vurdering>,
    ) : BehandlingVilkårsvurdert {
        fun iverksett(saksbehandler: Saksbehandler): BehandlingIverksatt.Innvilget {
            return BehandlingIverksatt.Innvilget(
                id = id,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
                vilkårsvurderinger = vilkårsvurderinger,
                saksbehandler = saksbehandler.navIdent,
            )
        }

        override fun leggTilSaksopplysning(saksopplysning: Saksopplysning): Søknadsbehandling {
            return this.copy(
                saksopplysninger = saksopplysninger + saksopplysning,
            )
        }
    }

    data class Avslag(
        override val id: BehandlingId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,
        override val vilkårsvurderinger: List<Vurdering>,
    ) : BehandlingVilkårsvurdert {
        fun iverksett(saksbehandler: Saksbehandler): BehandlingIverksatt.Avslag {
            TODO()
        }

        override fun leggTilSaksopplysning(saksopplysning: Saksopplysning): Søknadsbehandling {
            return this.copy(
                saksopplysninger = saksopplysninger + saksopplysning,
            )
        }
    }

    data class Manuell(
        override val id: BehandlingId,
        override val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
        override val saksopplysninger: List<Saksopplysning>,
        override val vilkårsvurderinger: List<Vurdering>,
    ) : BehandlingVilkårsvurdert {
        fun vurderPåNytt(saksopplysninger: List<Saksopplysning>): BehandlingVilkårsvurdert {
            return Søknadsbehandling.Opprettet(
                id = id,
                søknader = søknader,
                vurderingsperiode = vurderingsperiode,
                saksopplysninger = saksopplysninger,
            ).vilkårsvurder(saksopplysninger)
        }

        override fun leggTilSaksopplysning(saksopplysning: Saksopplysning): Søknadsbehandling {
            return this.copy(
                saksopplysninger = saksopplysninger + saksopplysning,
            )
        }
    }
}
