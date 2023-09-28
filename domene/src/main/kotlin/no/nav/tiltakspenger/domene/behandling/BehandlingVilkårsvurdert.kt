package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.vedtak.Innsending
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.toDTO
import no.nav.tiltakspenger.vilkårsvurdering.Vurdering

sealed interface BehandlingVilkårsvurdert : Søknadsbehandling {
    val vilkårsvurderinger: List<Vurdering>

    override fun søknad(): Søknad {
        return søknader.maxBy { it.id }
    }

    override fun toDTO(): BehandlingDTO {
        return BehandlingDTO(
            behandlingId = this.id.toString(),
            fom = this.vurderingsperiode.fra,
            tom = this.vurderingsperiode.til,
            søknad = this.søknad().toDTO(),
            saksopplysninger = this.saksopplysninger,
            vurderinger = this.vilkårsvurderinger,
            personopplysninger = PersonopplysningerDTO(
                ident = "12345678901",
                fornavn = "Ola",
                etternavn = "Nordmann",
                skjerming = false,
                strengtFortrolig = false,
                fortrolig = false,
            ),
        )
    }

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
        override val innsending: Innsending?,
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

        override fun leggTilSaksopplysning(saksopplysning: Saksopplysning): Søknadsbehandling {
            return this.copy(
                saksopplysninger = saksopplysninger + saksopplysning,
            )
        }
    }
}
