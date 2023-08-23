package no.nav.tiltakspenger.domene

import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Søknad
import java.util.UUID

sealed class Behandling {
    abstract val vurderingsperiode: Periode

    data class Opprettet(
        val id: BehandlingId,
        val søknader: List<Søknad>,
        override val vurderingsperiode: Periode,
    ) : Behandling() {
        companion object {
            fun lagBehandling(søknad: Søknad): Behandling {
                return Opprettet(
                    id = BehandlingId.random(),
                    søknader = listOf(søknad),
                    vurderingsperiode = søknad.vurderingsperiode(),
                )
            }
        }

        fun vedta(): Iverksatt {
            val nyttVedtak = Vedtak(UUID.randomUUID())

            return Iverksatt.Innvilget(
                id = id,
                vurderingsperiode = vurderingsperiode,
                søknader = søknader,
                vedtak = listOf(nyttVedtak),
            )
        }
    }

    sealed class Iverksatt {
        abstract val vurderingsperiode: Periode
        abstract val vedtak: List<Vedtak>

        data class Innvilget(
            val id: BehandlingId,
            val søknader: List<Søknad>,
            override val vurderingsperiode: Periode,
            override val vedtak: List<Vedtak> = emptyList(),
        ) : Iverksatt() {

        }

        data class Avslag(
            val id: BehandlingId,
            val søknader: List<Søknad>,
            override val vurderingsperiode: Periode,
            override val vedtak: List<Vedtak> = emptyList(),
        ) : Iverksatt() {

        }
    }
}


//val innhentedeRådata: Innsending,
//val avklarteSaksopplysninger: List<Saksopplysning>,
//val vilkårsvurderinger: List<Vilkårsvurdering>,
