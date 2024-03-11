package no.nav.tiltakspenger.domene.behandling

import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysning
import no.nav.tiltakspenger.domene.saksopplysning.Saksopplysninger.oppdaterSaksopplysninger
import no.nav.tiltakspenger.domene.vilkår.Vurdering
import no.nav.tiltakspenger.felles.BehandlingId
import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SakId

sealed interface BehandlingIverksatt : Søknadsbehandling {
    val vilkårsvurderinger: List<Vurdering>
    val beslutter: String

    override fun søknad(): Søknad {
        return søknader.siste()
    }

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
            beslutter: String,
        ): BehandlingIverksatt {
            return when (status) {
                "Innvilget" -> Innvilget(
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

                "Avslag" -> return Avslag(
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

                else -> throw IllegalStateException("Ukjent BehandlingVilkårsvurdert $id med status $status")
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
        override val beslutter: String,
    ) : BehandlingIverksatt {
        // trenger denne funksjoner?

        override fun leggTilSaksopplysning(saksopplysning: Saksopplysning): LeggTilSaksopplysningRespons {
            val oppdatertSaksopplysningListe = saksopplysninger.oppdaterSaksopplysninger(saksopplysning)
            return if (oppdatertSaksopplysningListe == this.saksopplysninger) {
                LeggTilSaksopplysningRespons(
                    behandling = this,
                    erEndret = false,
                )
            } else {
                // todo() her må vi lage en revurdering
                val nyBehandling = Søknadsbehandling.Opprettet.fromDb(
                    id = BehandlingId.random(),
                    sakId = this.sakId,
                    søknader = listOf(this.søknad()),
                    vurderingsperiode = this.vurderingsperiode,
                    saksopplysninger = oppdatertSaksopplysningListe,
                    tiltak = this.tiltak,
                    saksbehandler = null,
                ).vilkårsvurder()
                LeggTilSaksopplysningRespons(
                    behandling = nyBehandling,
                    erEndret = true,
                )
            }
        }

        override fun erTilBeslutter() = true
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
        override val beslutter: String,
    ) : BehandlingIverksatt {
        // trenger denne funksjoner?

        override fun leggTilSaksopplysning(saksopplysning: Saksopplysning): LeggTilSaksopplysningRespons {
            val oppdatertSaksopplysningListe = saksopplysninger.oppdaterSaksopplysninger(saksopplysning)
            return if (oppdatertSaksopplysningListe == this.saksopplysninger) {
                LeggTilSaksopplysningRespons(
                    behandling = this,
                    erEndret = false,
                )
            } else {
                // todo() her må vi lage en revurdering
                val nyBehandling = Søknadsbehandling.Opprettet.fromDb(
                    id = BehandlingId.random(),
                    sakId = this.sakId,
                    søknader = listOf(this.søknad()),
                    vurderingsperiode = this.vurderingsperiode,
                    saksopplysninger = oppdatertSaksopplysningListe,
                    tiltak = this.tiltak,
                    saksbehandler = null,
                ).vilkårsvurder()
                LeggTilSaksopplysningRespons(
                    behandling = nyBehandling,
                    erEndret = true,
                )
            }
        }

        override fun erTilBeslutter() = true
    }
}
